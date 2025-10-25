package com.wallet.mapper;

import com.wallet.entity.Wallet;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface WalletMapper {

    // 根据用户和币种查询钱包
    @Select("SELECT * FROM wallet WHERE user_id = #{userId} AND currency = #{currency}")
    Wallet selectByUserAndCurrency(@Param("userId") Long userId, @Param("currency") String currency);

    // 根据用户ID查询所有钱包
    @Select("SELECT * FROM wallet WHERE user_id = #{userId} ORDER BY currency")
    List<Wallet> selectByUserId(@Param("userId") Long userId);

    // 插入新钱包
    @Insert("INSERT INTO wallet(user_id, currency, balance, frozen_balance, version, status) " +
            "VALUES(#{userId}, #{currency}, #{balance}, #{frozenBalance}, #{version}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Wallet wallet);

    // 增加余额（无锁）
    @Update("UPDATE wallet SET balance = balance + #{amount}, version = version + 1, " +
            "updated_at = NOW() WHERE user_id = #{userId} AND currency = #{currency}")
    int updateBalance(@Param("userId") Long userId, @Param("currency") String currency,
                      @Param("amount") BigDecimal amount);

    // 减少余额（乐观锁防双花）
    @Update("UPDATE wallet SET balance = balance - #{amount}, version = version + 1, " +
            "updated_at = NOW() WHERE user_id = #{userId} AND currency = #{currency} " +
            "AND balance >= #{amount} AND version = #{version}")
    int decreaseBalance(@Param("userId") Long userId, @Param("currency") String currency,
                        @Param("amount") BigDecimal amount, @Param("version") Integer version);
}