package com.wallet.service;

import com.wallet.dto.response.TransactionResponse;
import com.wallet.entity.Wallet;
import com.wallet.exception.ConcurrentUpdateException;
import com.wallet.exception.InsufficientBalanceException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WalletServiceTest {

    @Autowired
    private WalletService walletService;

    /**
     * 正常充值功能
     */
    @Test
    void testRecharge_Success() {
        // Given
        Long userId = 1001L;
        String currency = "CNY";
        BigDecimal amount = new BigDecimal("500.00");
        String businessType = "TEST_RECHARGE";
        String businessId = "TEST_R001";

        // When
        TransactionResponse response = walletService.recharge(
                userId, currency, amount, businessType, businessId, "测试充值");

        // Then
        assertTrue(response.getSuccess());
        assertEquals("充值成功", response.getMessage());
        assertNotNull(response.getTransactionNo());
        assertEquals(0, new BigDecimal("1000.00").compareTo(response.getBalanceBefore()));
        assertEquals(0, new BigDecimal("1500.00").compareTo(response.getBalanceAfter()));

        // 验证余额确实更新了
        Wallet wallet = walletService.getWallet(userId, currency);
        assertEquals(0, new BigDecimal("1500.00").compareTo(wallet.getBalance()));
    }

    /**
     * 防止重复入账（幂等性）
     */
    @Test
    void testRecharge_Idempotent() {
        // Given
        Long userId = 1001L;
        String currency = "CNY";
        BigDecimal amount = new BigDecimal("200.00");
        String businessType = "TEST_IDEMPOTENT";
        String businessId = "TEST_ID001";

        // 第一次充值
        TransactionResponse firstResponse = walletService.recharge(
                userId, currency, amount, businessType, businessId, "第一次充值");

        // When - 第二次相同业务ID的充值
        TransactionResponse secondResponse = walletService.recharge(
                userId, currency, amount, businessType, businessId, "第二次充值");

        // Then
        assertTrue(firstResponse.getSuccess());
        assertTrue(secondResponse.getSuccess());
        assertEquals(firstResponse.getTransactionNo(), secondResponse.getTransactionNo());
        assertEquals("重复请求，返回之前成功结果", secondResponse.getMessage());

        // 验证余额只增加了一次
        Wallet wallet = walletService.getWallet(userId, currency);
        assertEquals(0, new BigDecimal("1200.00").compareTo(wallet.getBalance())); // 1000 + 200
    }

    /**
     * 正常提现功能
     */
    @Test
    void testWithdraw_Success() {
        // Given
        Long userId = 1001L;
        String currency = "CNY";
        BigDecimal amount = new BigDecimal("300.00");
        String businessType = "TEST_WITHDRAW";
        String businessId = "TEST_W001";

        // When
        TransactionResponse response = walletService.withdraw(
                userId, currency, amount, businessType, businessId, "测试提现");

        // Then
        assertTrue(response.getSuccess());
        assertEquals("提现成功", response.getMessage());
        assertEquals(0, new BigDecimal("1000.00").compareTo(response.getBalanceBefore()));
        assertEquals(0, new BigDecimal("700.00").compareTo(response.getBalanceAfter()));

        // 验证余额确实减少了
        Wallet wallet = walletService.getWallet(userId, currency);
        assertEquals(0, new BigDecimal("700.00").compareTo(wallet.getBalance()));
    }

    /**
     * 防止双花（余额不足）
     */
    @Test
    void testWithdraw_InsufficientBalance() {
        // Given
        Long userId = 1001L;
        String currency = "CNY";
        BigDecimal amount = new BigDecimal("2000.00"); // 超过余额
        String businessType = "TEST_OVERDRAFT";
        String businessId = "TEST_OD001";

        // When & Then
        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> walletService.withdraw(userId, currency, amount, businessType, businessId, "超额提现")
        );

        assertTrue(exception.getMessage().contains("余额不足"));
    }

    /**
     * 新用户自动创建钱包
     */
    @Test
    void testRecharge_NewUserWallet() {
        // Given - 新用户
        Long userId = 9999L;
        String currency = "CNY";
        BigDecimal amount = new BigDecimal("1000.00");
        String businessType = "TEST_NEW_USER";
        String businessId = "TEST_NU001";

        // When
        TransactionResponse response = walletService.recharge(
                userId, currency, amount, businessType, businessId, "新用户充值");

        // Then
        assertTrue(response.getSuccess());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getBalanceBefore()));
        assertEquals(0, amount.compareTo(response.getBalanceAfter()));

        // 验证钱包已创建
        Wallet wallet = walletService.getWallet(userId, currency);
        assertNotNull(wallet);
        assertEquals(0, amount.compareTo(wallet.getBalance()));

    }

    /**
     * 防双花（并发控制）
     */
    @Test
    void testWithdraw_ConcurrentUpdate() throws InterruptedException {
        // Given
        Long userId = 1001L;
        String currency = "CNY";
        BigDecimal amount = new BigDecimal("800.00");
        int threadCount = 3;

        java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch endLatch = new java.util.concurrent.CountDownLatch(threadCount);

        java.util.List<TestResult> results = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

        // When - 并发执行
        for (int i = 0; i < threadCount; i++) {
            int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await(); // 等待同时开始

                    String businessId = "CONCURRENT_" + threadId + "_" + System.currentTimeMillis();
                    TestResult result = new TestResult(threadId, businessId);

                    try {
                        long startTime = System.currentTimeMillis();
                        TransactionResponse response = walletService.withdraw(
                                userId, currency, amount,
                                "CONCURRENT_TEST", businessId, "并发测试");
                        long endTime = System.currentTimeMillis();

                        result.setSuccess(response.getSuccess());
                        result.setExecutionTime(endTime - startTime);
                        result.setMessage("成功");
                    } catch (ConcurrentUpdateException e) {
                        result.setSuccess(false);
                        result.setMessage("并发冲突");
                    } catch (Exception e) {
                        result.setSuccess(false);
                        result.setMessage(e.getMessage());
                    }

                    results.add(result);

                } catch (Exception e) {
                    log.error("线程 {} 初始化或等待异常: {}", threadId, e.getMessage(), e);
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        // 同时启动所有线程
        startLatch.countDown();

        // 等待所有线程完成
        endLatch.await();

        // Then
        long successCount = results.stream().filter(TestResult::isSuccess).count();
        long conflictCount = results.stream().filter(r -> "并发冲突".equals(r.getMessage())).count();

        assertEquals(1, successCount); // 只有一个成功
        assertEquals(threadCount - 1, conflictCount); // 其他都冲突
    }

    // 测试结果内部类
    @Data
    static class TestResult {
        int threadId;
        String businessId;
        boolean success;
        long executionTime;
        String message;

        public TestResult(int threadId, String businessId) {
            this.threadId = threadId;
            this.businessId = businessId;
        }
    }
}