package com.wallet.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TransactionRecordResponse {
    private String transactionNo;   // 交易流水号
    private String currency;        // 币种
    private BigDecimal amount;      // 交易金额
    private String amountDisplay;   // 金额显示（带符号）
    private BigDecimal balanceBefore; // 交易前余额
    private BigDecimal balanceAfter;  // 交易后余额
    private String transactionType; // 交易类型描述
    private String businessType;    // 业务类型
    private String businessId;      // 业务ID
    private String status;          // 状态描述
    private String remark;          // 备注
    private Date createdAt;         // 创建时间

    public String getAmountDisplay() {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + amount + " " + currency;
        } else {
            return amount + " " + currency;
        }
    }
}