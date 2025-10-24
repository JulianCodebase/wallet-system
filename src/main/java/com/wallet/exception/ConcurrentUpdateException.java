package com.wallet.exception;

// 并发更新异常
public class ConcurrentUpdateException extends BusinessException {
    public ConcurrentUpdateException(String message) {
        super("CONCURRENT_UPDATE", message);
    }
}
