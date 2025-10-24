package com.wallet.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawRequest {
    private Long userId;
    private String currency;
    private BigDecimal amount;
    private String businessType;
    private String businessId;
    private String remark;
}
