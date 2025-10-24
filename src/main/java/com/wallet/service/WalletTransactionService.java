package com.wallet.service;

import com.wallet.entity.WalletTransaction;
import com.wallet.mapper.WalletTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletTransactionService {

    private final WalletTransactionMapper walletTransactionMapper;

    /**
     * 查询用户交易记录
     */
    public List<WalletTransaction> getTransactionsByUser(Long userId, String currency, Date startTime, Date endTime) {
        log.info("查询用户交易记录: userId={}, currency={}, startTime={}, endTime={}",
                userId, currency, startTime, endTime);

        return walletTransactionMapper.selectByUserAndTime(userId, currency, startTime, endTime);
    }

    /**
     * 根据业务标识查询交易记录（用于幂等检查）
     */
    public WalletTransaction getTransactionByBusiness(String businessType, String businessId) {
        return walletTransactionMapper.selectByBusiness(businessType, businessId);
    }
}