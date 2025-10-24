package com.wallet.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BalanceChangeHistory {
    private Long id;
    private Long walletId;          // 钱包ID
    private Long transactionId;     // 交易ID
    private BigDecimal changeAmount; // 变更金额
    private BigDecimal balanceBefore; // 变更前余额
    private BigDecimal balanceAfter;  // 变更后余额
    private String changeType;      // 变更类型:BALANCE,FROZEN
    private Date createdAt;
}