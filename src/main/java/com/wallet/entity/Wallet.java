package com.wallet.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Wallet {
    private Long id;
    private Long userId;           // 用户ID
    private String currency;       // 币种: USD,CNY,BTC,ETH
    private BigDecimal balance;    // 余额
    private BigDecimal frozenBalance; // 冻结余额
    private Integer version;       // 版本号，用于乐观锁
    private Integer status;        // 状态:1正常,0冻结
    private Date createdAt;
    private Date updatedAt;
}