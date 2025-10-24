package com.wallet.mapper;

import com.wallet.entity.BalanceChangeHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface BalanceChangeHistoryMapper {

    @Insert("INSERT INTO balance_change_history(wallet_id, transaction_id, change_amount, " +
            "balance_before, balance_after, change_type) " +
            "VALUES(#{walletId}, #{transactionId}, #{changeAmount}, " +
            "#{balanceBefore}, #{balanceAfter}, #{changeType})")
    int insert(BalanceChangeHistory history);

    @Select("SELECT * FROM balance_change_history WHERE wallet_id = #{walletId} " +
            "AND created_at BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY created_at")
    List<BalanceChangeHistory> selectByWalletAndTime(@Param("walletId") Long walletId,
                                                     @Param("startTime") Date startTime,
                                                     @Param("endTime") Date endTime);
}