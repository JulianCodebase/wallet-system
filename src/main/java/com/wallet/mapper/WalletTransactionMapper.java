package com.wallet.mapper;

import com.wallet.entity.WalletTransaction;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Mapper
public interface WalletTransactionMapper {

    // 插入交易记录
    @Insert("INSERT INTO wallet_transaction(transaction_no, wallet_id, user_id, currency, amount, " +
            "balance_before, balance_after, transaction_type, business_type, business_id, status, remark) " +
            "VALUES(#{transactionNo}, #{walletId}, #{userId}, #{currency}, #{amount}, " +
            "#{balanceBefore}, #{balanceAfter}, #{transactionType}, #{businessType}, #{businessId}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WalletTransaction transaction);

    // 根据业务标识查询交易记录（幂等检查）
    @Select("SELECT * FROM wallet_transaction WHERE business_type = #{businessType} " +
            "AND business_id = #{businessId}")
    WalletTransaction selectByBusiness(@Param("businessType") String businessType,
                                       @Param("businessId") String businessId);

    // 根据ID更新交易状态
    @Update("UPDATE wallet_transaction SET status = #{status}, balance_after = #{balanceAfter}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status,
                     @Param("balanceAfter") BigDecimal balanceAfter);

    // 查询用户交易记录
    List<WalletTransaction> selectByUserAndTime(@Param("userId") Long userId,
                                                @Param("currency") String currency,
                                                @Param("startTime") Date startTime,
                                                @Param("endTime") Date endTime);
}