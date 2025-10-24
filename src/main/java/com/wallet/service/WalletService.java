package com.wallet.service;

import com.wallet.dto.response.TransactionResponse;
import com.wallet.entity.BalanceChangeHistory;
import com.wallet.entity.Wallet;
import com.wallet.entity.WalletTransaction;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import com.wallet.exception.ConcurrentUpdateException;
import com.wallet.exception.InsufficientBalanceException;
import com.wallet.mapper.BalanceChangeHistoryMapper;
import com.wallet.mapper.WalletMapper;
import com.wallet.mapper.WalletTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletMapper walletMapper;

    private final WalletTransactionMapper transactionMapper;

    private final BalanceChangeHistoryMapper balanceChangeHistoryMapper;

    /**
     * 充值操作
     * @param userId 用户ID
     * @param currency 币种
     * @param amount 金额
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param remark 备注
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public TransactionResponse recharge(Long userId, String currency, BigDecimal amount,
                                        String businessType, String businessId, String remark) {
        log.info("用户充值: userId={}, currency={}, amount={}, business={}/{}",
                userId, currency, amount, businessType, businessId);

        // 1. 幂等检查 - 防止重复入账
        TransactionResponse idempotentResponse = checkIdempotentAndReturnResponse(userId, currency, amount,
                businessType, businessId, remark, false);
        if (idempotentResponse != null) {
            return idempotentResponse;
        }

        // 2. 先获取或创建钱包
        Wallet wallet = walletMapper.selectByUserAndCurrency(userId, currency);
        if (wallet == null) {
            log.info("钱包不存在，创建新钱包: userId={}, currency={}", userId, currency);
            createWallet(userId, currency);
            wallet = walletMapper.selectByUserAndCurrency(userId, currency);
        }

        // 3. 计算交易后的余额
        BigDecimal balanceAfter = wallet.getBalance().add(amount);

        // 4. 创建交易记录
        String transactionNo = generateTransactionNo();
        WalletTransaction transaction = createTransaction(userId, currency, amount,
                transactionNo, businessType, businessId, TransactionType.RECHARGE, remark);
        transaction.setWalletId(wallet.getId());
        transaction.setBalanceBefore(wallet.getBalance());
        transaction.setBalanceAfter(balanceAfter);
        transactionMapper.insert(transaction);

        try {
            // 5. 更新余额
            int rows = walletMapper.updateBalance(userId, currency, amount);
            if (rows == 0) {
                throw new RuntimeException("更新余额失败，请重试");
            }

            // 6. 更新交易记录为成功状态
            transaction.setStatus(TransactionStatus.SUCCESS.getCode());
            transactionMapper.updateStatus(transaction.getId(), transaction.getStatus(), balanceAfter);

            // 7. 记录余额变更历史
            recordBalanceChange(transaction, wallet, balanceAfter);

            log.info("充值成功: userId={}, currency={}, amount={}, newBalance={}",
                    userId, currency, amount, balanceAfter);

            // 构建响应对象
            TransactionResponse response = new TransactionResponse();
            response.setSuccess(true);
            response.setMessage("充值成功");
            response.setTransactionNo(transactionNo);
            response.setUserId(userId);
            response.setCurrency(currency);
            response.setAmount(amount);
            response.setBalanceBefore(wallet.getBalance());
            response.setBalanceAfter(balanceAfter);
            response.setBusinessType(businessType);
            response.setBusinessId(businessId);
            response.setRemark(remark);
            return response;

        } catch (Exception e) {
            log.error("充值失败: userId={}, currency={}, amount={}, error={}",
                    userId, currency, amount, e.getMessage(), e);
            transaction.setStatus(TransactionStatus.FAILED.getCode());
            transactionMapper.updateStatus(transaction.getId(), transaction.getStatus(), balanceAfter);
            throw e;
        }
    }

    /**
     * 提现操作（防双花核心）
     * @param userId 用户ID
     * @param currency 币种
     * @param amount 金额
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param remark 备注
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public TransactionResponse withdraw(Long userId, String currency, BigDecimal amount,
                                        String businessType, String businessId, String remark) {
        log.info("用户提现: userId={}, currency={}, amount={}, business={}/{}",
                userId, currency, amount, businessType, businessId);

        // 1. 幂等检查
        TransactionResponse idempotentResponse = checkIdempotentAndReturnResponse(userId, currency, amount,
                businessType, businessId, remark, true);
        if (idempotentResponse != null) {
            return idempotentResponse;
        }

        // 2. 查询钱包并检查余额
        Wallet wallet = walletMapper.selectByUserAndCurrency(userId, currency);
        if (wallet == null) {
            throw new InsufficientBalanceException("钱包不存在");
        }
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("余额不足，当前余额: " + wallet.getBalance());
        }

        // 3. 计算交易后的余额
        BigDecimal balanceAfter = wallet.getBalance().subtract(amount);

        // 4. 创建交易记录
        String transactionNo = generateTransactionNo();
        WalletTransaction transaction = createTransaction(userId, currency, amount.negate(),
                transactionNo, businessType, businessId, TransactionType.WITHDRAW, remark);
        transaction.setWalletId(wallet.getId());
        transaction.setBalanceBefore(wallet.getBalance());
        transaction.setBalanceAfter(balanceAfter);
        transactionMapper.insert(transaction);

        try {
            // 5. 扣减余额
            int rows = walletMapper.decreaseBalance(userId, currency, amount, wallet.getVersion());
            if (rows == 0) {
                throw new ConcurrentUpdateException("并发操作冲突，请重试");
            }

            // 6. 更新交易记录状态
            transaction.setStatus(TransactionStatus.SUCCESS.getCode());
            transactionMapper.updateStatus(transaction.getId(), transaction.getStatus(), balanceAfter);

            // 7. 记录余额变更历史
            recordBalanceChange(transaction, wallet, balanceAfter);

            log.info("提现成功: userId={}, currency={}, amount={}, newBalance={}",
                    userId, currency, amount, balanceAfter);

            // 构建响应对象
            TransactionResponse response = new TransactionResponse();
            response.setSuccess(true);
            response.setMessage("提现成功");
            response.setTransactionNo(transactionNo);
            response.setUserId(userId);
            response.setCurrency(currency);
            response.setAmount(amount.negate()); // 负数表示支出
            response.setBalanceBefore(wallet.getBalance());
            response.setBalanceAfter(balanceAfter);
            response.setBusinessType(businessType);
            response.setBusinessId(businessId);
            response.setRemark(remark);
            return response;

        } catch (Exception e) {
            log.error("提现失败: userId={}, currency={}, amount={}, error={}",
                    userId, currency, amount, e.getMessage(), e);
            transaction.setStatus(TransactionStatus.FAILED.getCode());
            transactionMapper.updateStatus(transaction.getId(), transaction.getStatus(), balanceAfter);
            throw e;
        }
    }

    /**
     * 查询钱包余额
     */
    public Wallet getWallet(Long userId, String currency) {
        return walletMapper.selectByUserAndCurrency(userId, currency);
    }

    /**
     * 幂等检查并返回响应
     * @param userId 用户ID
     * @param currency 币种
     * @param amount 金额
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param remark 备注
     * @param isWithdraw 是否为提现操作
     * @return 如果存在重复请求则返回响应，否则返回null
     */
    private TransactionResponse checkIdempotentAndReturnResponse(Long userId, String currency, BigDecimal amount,
                                                                 String businessType, String businessId,
                                                                 String remark, boolean isWithdraw) {
        WalletTransaction existingTransaction = transactionMapper.selectByBusiness(businessType, businessId);
        if (existingTransaction != null) {
            log.info("重复业务请求，直接返回之前结果: business={}/{}, status={}",
                    businessType, businessId, existingTransaction.getStatus());

            TransactionResponse response = new TransactionResponse();
            response.setSuccess(existingTransaction.getStatus().equals(TransactionStatus.SUCCESS.getCode()));
            response.setMessage(response.getSuccess() ? "重复请求，返回之前成功结果" : "重复请求，返回之前失败结果");
            response.setTransactionNo(existingTransaction.getTransactionNo());
            response.setUserId(userId);
            response.setCurrency(currency);
            response.setAmount(isWithdraw ? amount.negate() : amount); // 提现为负数，充值为正数
            response.setBalanceBefore(existingTransaction.getBalanceBefore());
            response.setBalanceAfter(existingTransaction.getBalanceAfter());
            response.setBusinessType(businessType);
            response.setBusinessId(businessId);
            response.setRemark(remark);
            return response;
        }
        return null; // 返回null表示没有重复请求，需要继续处理
    }

    /**
     * 幂等检查 - 防止重复业务请求
     */
    private Boolean checkIdempotent(String businessType, String businessId) {
        WalletTransaction existingTransaction = transactionMapper.selectByBusiness(businessType, businessId);
        if (existingTransaction != null) {
            log.info("重复业务请求，直接返回之前结果: business={}/{}, status={}",
                    businessType, businessId, existingTransaction.getStatus());
            return existingTransaction.getStatus().equals(TransactionStatus.SUCCESS.getCode());
        }
        return null; // 返回null表示没有重复请求，需要继续处理
    }

    /**
     * 创建新钱包
     */
    private void createWallet(Long userId, String currency) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setCurrency(currency);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setFrozenBalance(BigDecimal.ZERO);
        wallet.setVersion(0);
        wallet.setStatus(1);
        walletMapper.insert(wallet);
    }

    /**
     * 创建交易记录对象
     */
    private WalletTransaction createTransaction(Long userId, String currency, BigDecimal amount,
                                                String transactionNo, String businessType,
                                                String businessId, TransactionType type, String remark) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setTransactionNo(transactionNo);
        transaction.setUserId(userId);
        transaction.setCurrency(currency);
        transaction.setAmount(amount);
        transaction.setBusinessType(businessType);
        transaction.setBusinessId(businessId);
        transaction.setTransactionType(type.getCode());
        transaction.setStatus(TransactionStatus.PROCESSING.getCode());
        transaction.setRemark(remark);
        return transaction;
    }

    /**
     * 记录余额变更历史
     */
    private void recordBalanceChange(WalletTransaction transaction, Wallet wallet, BigDecimal balanceAfter) {
        BalanceChangeHistory history = new BalanceChangeHistory();
        history.setWalletId(wallet.getId());
        history.setTransactionId(transaction.getId());
        history.setChangeAmount(transaction.getAmount());
        history.setBalanceBefore(transaction.getBalanceBefore());
        history.setBalanceAfter(balanceAfter);
        history.setChangeType("BALANCE");
        balanceChangeHistoryMapper.insert(history);
    }

    /**
     * 生成交易流水号
     */
    private String generateTransactionNo() {
        return "T" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}