-- 钱包表
CREATE TABLE wallet
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT         NOT NULL,
    currency       VARCHAR(10)    NOT NULL,
    balance        DECIMAL(20, 8) NOT NULL DEFAULT 0,
    frozen_balance DECIMAL(20, 8) NOT NULL DEFAULT 0,
    version        INT            NOT NULL DEFAULT 0,
    status         TINYINT        NOT NULL DEFAULT 1,
    created_at     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 交易流水表
CREATE TABLE wallet_transaction
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_no   VARCHAR(64)    NOT NULL,
    wallet_id        BIGINT         NOT NULL,
    user_id          BIGINT         NOT NULL,
    currency         VARCHAR(10)    NOT NULL,
    amount           DECIMAL(20, 8) NOT NULL,
    balance_before   DECIMAL(20, 8) NOT NULL DEFAULT 0,
    balance_after    DECIMAL(20, 8) NOT NULL DEFAULT 0,
    transaction_type TINYINT        NOT NULL,
    business_type    VARCHAR(50)    NOT NULL,
    business_id      VARCHAR(64)    NOT NULL,
    status           TINYINT        NOT NULL DEFAULT 0,
    remark           VARCHAR(500),
    created_at       TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 余额变更历史表
CREATE TABLE balance_change_history
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_id      BIGINT         NOT NULL,
    transaction_id BIGINT         NOT NULL,
    change_amount  DECIMAL(20, 8) NOT NULL,
    balance_before DECIMAL(20, 8) NOT NULL,
    balance_after  DECIMAL(20, 8) NOT NULL,
    change_type    VARCHAR(20)    NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);