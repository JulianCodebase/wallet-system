package com.wallet.dto.request;

import lombok.Data;

import java.math.BigDecimal;

// 请求对象
@Data
public class RechargeRequest {
    private Long userId;
    private String currency;
    private BigDecimal amount;
    private String businessType;
    private String businessId;
    private String remark;
}
