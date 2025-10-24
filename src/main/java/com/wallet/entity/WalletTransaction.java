package com.wallet.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class WalletTransaction {
    private Long id;
    private String transactionNo;   // 交易流水号，全局唯一
    private Long walletId;          // 钱包ID
    private Long userId;            // 用户ID
    private String currency;        // 币种
    private BigDecimal amount;      // 交易金额，正数表示收入，负数表示支出
    private BigDecimal balanceBefore; // 交易前余额
    private BigDecimal balanceAfter;  // 交易后余额
    private Integer transactionType; // 交易类型:1充值,2提现,3转账,4消费
    private String businessType;    // 业务类型
    private String businessId;      // 业务ID，用于幂等
    private Integer status;         // 状态:1成功,2失败,0处理中
    private String remark;          // 备注
    private Date createdAt;
    private Date updatedAt;
}