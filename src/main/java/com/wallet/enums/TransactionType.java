package com.wallet.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum TransactionType {
    RECHARGE(1, "充值"),
    WITHDRAW(2, "提现"),
    TRANSFER(3, "转账"),
    CONSUME(4, "消费");

    private final Integer code;
    private final String desc;

    TransactionType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Optional<TransactionType> fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(type -> type.getCode().equals(code))
                .findFirst();
    }
}