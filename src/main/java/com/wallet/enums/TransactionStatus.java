package com.wallet.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum TransactionStatus {
    PROCESSING(0, "处理中"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败");

    private final Integer code;
    private final String desc;

    TransactionStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Optional<TransactionStatus> fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(status -> status.getCode().equals(code))
                .findFirst();
    }
}