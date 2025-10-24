package com.wallet.dto.converter;

import com.wallet.dto.response.WalletResponse;
import com.wallet.dto.response.TransactionRecordResponse;
import com.wallet.entity.Wallet;
import com.wallet.entity.WalletTransaction;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WalletConverter {

    public WalletResponse toWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setUserId(wallet.getUserId());
        response.setCurrency(wallet.getCurrency());
        response.setBalance(wallet.getBalance());
        response.setFrozenBalance(wallet.getFrozenBalance());
        response.setStatus(wallet.getStatus());
        response.setStatusDesc(wallet.getStatus() == 1 ? "正常" : "冻结");
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