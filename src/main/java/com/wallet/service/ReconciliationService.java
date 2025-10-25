package com.wallet.service;

import com.wallet.dto.response.ReconciliationResult;
import com.wallet.entity.BalanceChangeHistory;
import com.wallet.entity.Wallet;
import com.wallet.exception.BusinessException;
import com.wallet.mapper.BalanceChangeHistoryMapper;
import com.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final BalanceChangeHistoryMapper historyMapper;

    private final WalletMapper walletMapper;

    private final WalletService walletService;

    /**
     * 对账查询 - 基于用户ID和币种
     */
    public ReconciliationResult reconcile(Long userId, String currency, Date startTime, Date endTime) {
        // 1. 先根据userId和currency获取钱包
        Wallet wallet = walletMapper.selectByUserAndCurrency(userId, currency);
        if (wallet == null) {
            throw new BusinessException("WALLET_NOT_FOUND", "钱包不存在");
        }

        Long walletId = wallet.getId();

        // 2. 查询期间所有余额变更记录
        List<BalanceChangeHistory> changes = historyMapper.selectByWalletAndTime(
                walletId, startTime, endTime);

        // 3. 获取期初余额（对账期开始时的余额）
        BigDecimal openingBalance = getOpeningBalance(walletId, startTime);

        // 4. 计算理论期末余额
        BigDecimal calculatedClosingBalance = calculateClosingBalance(openingBalance, changes);

        // 5. 获取实际期末余额（当前钱包余额）
        BigDecimal actualClosingBalance = wallet.getBalance();

        // 6. 验证是否一致
        boolean isBalanced = calculatedClosingBalance.compareTo(actualClosingBalance) == 0;

        return ReconciliationResult.builder()
                .userId(userId)
                .currency(currency)
                .walletId(walletId)
                .openingBalance(openingBalance)
                .calculatedClosingBalance(calculatedClosingBalance)
                .actualClosingBalance(actualClosingBalance)
                .isBalanced(isBalanced)
                .totalChanges(changes.size())
                .totalInflow(calculateTotalInflow(changes))
                .totalOutflow(calculateTotalOutflow(changes))
                .changes(changes)
                .build();
    }

    /**
     * 多币种对账查询
     */
    public List<ReconciliationResult> reconcileAll(Long userId, Date startTime, Date endTime) {
        // 通过WalletService获取钱包，而不是直接访问Mapper
        List<Wallet> wallets = walletService.getWalletsByUserId(userId);

        return wallets.stream()
                .map(wallet -> reconcile(userId, wallet.getCurrency(), startTime, endTime))
                .collect(Collectors.toList());
    }

    private BigDecimal getOpeningBalance(Long walletId, Date startTime) {
        // 查询对账期开始前最后一笔变更后的余额
        BalanceChangeHistory lastChange = historyMapper.selectLastChangeBefore(walletId, startTime);
        return lastChange != null ? lastChange.getBalanceAfter() : BigDecimal.ZERO;
    }

    private BigDecimal calculateClosingBalance(BigDecimal openingBalance,
                                               List<BalanceChangeHistory> changes) {
        BigDecimal balance = openingBalance;
        for (BalanceChangeHistory change : changes) {
            balance = balance.add(change.getChangeAmount());
        }
        return balance;
    }

    private BigDecimal calculateTotalInflow(List<BalanceChangeHistory> changes) {
        return changes.stream()
                .filter(change -> change.getChangeAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(BalanceChangeHistory::getChangeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalOutflow(List<BalanceChangeHistory> changes) {
        return changes.stream()
                .filter(change -> change.getChangeAmount().compareTo(BigDecimal.ZERO) < 0)
                .map(BalanceChangeHistory::getChangeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();  // 取绝对值
    }
}
