package com.wallet.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WalletResponse {
    private Long userId;           // 用户ID
    private String currency;       // 币种
    private BigDecimal balance;    // 余额
    private BigDecimal frozenBalance; // 冻结余额
    private Integer status;        // 状态
    private String statusDesc;     // 状态描述

}