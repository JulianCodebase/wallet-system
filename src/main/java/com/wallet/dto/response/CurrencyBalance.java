package com.wallet.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
public class CurrencyBalance {
    private String currency;           // 币种
    private BigDecimal balance;        // 余额
    private BigDecimal frozenBalance;  // 冻结余额
    private BigDecimal availableBalance; // 可用余额
    private Integer status;            // 状态
    private String statusDesc;         // 状态描述
    private String balanceDisplay;     // 格式化显示

    // 计算可用余额
    public BigDecimal getAvailableBalance() {
        if (balance == null || frozenBalance == null) {
            return BigDecimal.ZERO;
        }
        return balance.subtract(frozenBalance);
    }

    // 格式化显示余额
    public String getBalanceDisplay() {
        return balance != null ?
                balance.setScale(8, RoundingMode.HALF_UP).toPlainString() : "0.00000000";
    }

    // 状态描述
    public String getStatusDesc() {
        if (status == null) return "未知";
        return status == 1 ? "正常" : "冻结";
    }
}