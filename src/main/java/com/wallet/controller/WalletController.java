package com.wallet.controller;

import com.wallet.dto.converter.WalletConverter;
import com.wallet.dto.response.ApiResponse;
import com.wallet.dto.request.RechargeRequest;
import com.wallet.dto.response.TransactionRecordResponse;
import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.request.WithdrawRequest;
import com.wallet.dto.response.WalletResponse;
import com.wallet.entity.Wallet;
import com.wallet.entity.WalletTransaction;
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

    private final WalletConverter walletConverter;

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
    public ApiResponse<WalletResponse> getBalance(@RequestParam Long userId,
                                                  @RequestParam String currency) {
        try {
            Wallet wallet = walletService.getWallet(userId, currency);
            WalletResponse response = walletConverter.toWalletResponse(wallet);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("查询余额异常: {}", e.getMessage(), e);
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
            List<WalletTransaction> transactions = transactionService.getTransactionsByUser(
                    userId, currency, startTime, endTime);
            List<TransactionRecordResponse> responses = walletConverter.toTransactionRecordResponses(transactions);
            return ApiResponse.success(responses);
        } catch (Exception e) {
            log.error("查询交易记录异常: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
}

