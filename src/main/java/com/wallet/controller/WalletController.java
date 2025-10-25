package com.wallet.controller;

import com.wallet.dto.request.RechargeRequest;
import com.wallet.dto.request.WithdrawRequest;
import com.wallet.dto.response.*;
import com.wallet.service.WalletService;
import com.wallet.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    private final WalletTransactionService transactionService;

    /**
     * 充值接口 - 返回详细交易结果
     */
    @PostMapping("/recharge")
    public ApiResponse<TransactionResponse> recharge(@RequestBody RechargeRequest request) {
        try {
            TransactionResponse result = walletService.recharge(
                    request.getUserId(),
                    request.getCurrency(),
                    request.getAmount(),
                    request.getBusinessType(),
                    request.getBusinessId(),
                    request.getRemark()
            );
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("充值异常: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 提现接口 - 返回详细交易结果
     */
    @PostMapping("/withdraw")
    public ApiResponse<TransactionResponse> withdraw(@RequestBody WithdrawRequest request) {
        try {
            TransactionResponse result = walletService.withdraw(
                    request.getUserId(),
                    request.getCurrency(),
                    request.getAmount(),
                    request.getBusinessType(),
                    request.getBusinessId(),
                    request.getRemark()
            );
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("提现异常: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询余额
     */
    @GetMapping("/balance")
    public ApiResponse<CurrencyBalance> getBalance(@RequestParam Long userId,
                                                   @RequestParam String currency) {
        try {
            return ApiResponse.success(walletService.getCurrencyBalance(userId, currency));
        } catch (Exception e) {
            log.error("查询余额异常: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询用户所有币种余额汇总
     */
    @GetMapping("/balances")
    public ApiResponse<WalletSummaryResponse> getAllBalances(@RequestParam Long userId) {
        try {
            return ApiResponse.success(walletService.getWalletSummary(userId));
        } catch (Exception e) {
            log.error("查询余额汇总异常: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 交易记录查询
     */
    @GetMapping("/transactions")
    public ApiResponse<List<TransactionRecordResponse>> getTransactions(
            @RequestParam Long userId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
        try {
            List<TransactionRecordResponse> responses = transactionService.getTransactionRecords(
                    userId, currency, startTime, endTime);
            return ApiResponse.success(responses);
        } catch (Exception e) {
            log.error("查询交易记录异常: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
}

