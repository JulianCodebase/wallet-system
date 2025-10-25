package com.wallet.dto.response;

import com.wallet.entity.BalanceChangeHistory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ReconciliationResult {
    private Long userId;                        // 用户ID
    private String currency;                    // 币种
    private Long walletId;                      // 钱包ID
    private BigDecimal openingBalance;          // 期初余额
    private BigDecimal calculatedClosingBalance; // 计算期末余额
    private BigDecimal actualClosingBalance;    // 实际期末余额
    private Boolean isBalanced;                 // 是否平衡
    private Integer totalChanges;               // 总变更次数
    private BigDecimal totalInflow;             // 总流入
    private BigDecimal totalOutflow;            // 总流出
    private List<BalanceChangeHistory> changes; // 详细变更记录
}
