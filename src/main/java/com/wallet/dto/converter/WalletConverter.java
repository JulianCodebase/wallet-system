package com.wallet.dto.converter;

import com.wallet.dto.response.CurrencyBalance;
import com.wallet.dto.response.TransactionRecordResponse;
import com.wallet.dto.response.WalletSummaryResponse;
import com.wallet.entity.Wallet;
import com.wallet.entity.WalletTransaction;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WalletConverter {

    public CurrencyBalance toCurrencyBalance(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        CurrencyBalance balance = new CurrencyBalance();
        balance.setCurrency(wallet.getCurrency());
        balance.setBalance(wallet.getBalance());
        balance.setFrozenBalance(wallet.getFrozenBalance());
        balance.setStatus(wallet.getStatus());
        return balance;
    }

    public List<CurrencyBalance> toCurrencyBalances(List<Wallet> wallets) {
        return wallets.stream()
                .map(this::toCurrencyBalance)
                .collect(Collectors.toList());
    }

    public WalletSummaryResponse toWalletSummaryResponse(Long userId, List<Wallet> wallets) {
        WalletSummaryResponse response = new WalletSummaryResponse();
        response.setUserId(userId);
        response.setBalances(toCurrencyBalances(wallets));
        return response;
    }

    public TransactionRecordResponse toTransactionRecordResponse(WalletTransaction transaction) {
        TransactionRecordResponse response = new TransactionRecordResponse();
        response.setTransactionNo(transaction.getTransactionNo());
        response.setCurrency(transaction.getCurrency());
        response.setAmount(transaction.getAmount());
        response.setBalanceBefore(transaction.getBalanceBefore());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setTransactionType(getTransactionTypeDesc(transaction.getTransactionType()));
        response.setBusinessType(transaction.getBusinessType());
        response.setBusinessId(transaction.getBusinessId());
        response.setStatus(getStatusDesc(transaction.getStatus()));
        response.setRemark(transaction.getRemark());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }

    public List<TransactionRecordResponse> toTransactionRecordResponses(List<WalletTransaction> transactions) {
        return transactions.stream()
                .map(this::toTransactionRecordResponse)
                .collect(Collectors.toList());
    }

    private String getTransactionTypeDesc(Integer transactionType) {
        return TransactionType.fromCode(transactionType)
                .map(TransactionType::getDesc)
                .orElse("未知");
    }

    private String getStatusDesc(Integer status) {
        return TransactionStatus.fromCode(status)
                .map(TransactionStatus::getDesc)
                .orElse("未知");
    }
}