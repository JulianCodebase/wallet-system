package com.wallet.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionResponse {
    private Boolean success;
    private String message;
    private String transactionNo;      // 交易流水号
    private Long userId;               // 用户ID
    private String currency;           // 币种
    private BigDecimal amount;         // 交易金额
    private BigDecimal balanceBefore;  // 交易前余额
    private BigDecimal balanceAfter;   // 交易后余额
    private String businessType;       // 业务类型
    private String businessId;         // 业务ID
    private String remark;             // 备注
}