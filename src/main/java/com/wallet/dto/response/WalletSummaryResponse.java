package com.wallet.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class WalletSummaryResponse {
    private Long userId;
    private List<CurrencyBalance> balances;

    // 获取特定币种的余额
    public Optional<CurrencyBalance> getBalanceByCurrency(String currency) {
        return balances.stream()
                .filter(balance -> currency.equals(balance.getCurrency()))
                .findFirst();
    }
}
