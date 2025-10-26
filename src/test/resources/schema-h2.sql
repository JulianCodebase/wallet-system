-- 创建钱包表
CREATE TABLE wallet (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        user_id BIGINT NOT NULL,
                        currency VARCHAR(10) NOT NULL,
                        balance DECIMAL(20,8) NOT NULL DEFAULT 0,
                        frozen_balance DECIMAL(20,8) NOT NULL DEFAULT 0,
                        version INT NOT NULL DEFAULT 0,
                        status TINYINT NOT NULL DEFAULT 1,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建交易流水表
CREATE TABLE wallet_transaction (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    transaction_no VARCHAR(64) NOT NULL,
                                    wallet_id BIGINT NOT NULL,
                                    user_id BIGINT NOT NULL,
                                    currency VARCHAR(10) NOT NULL,
                                    amount DECIMAL(20,8) NOT NULL,
                                    balance_before DECIMAL(20,8) NOT NULL DEFAULT 0,
                                    balance_after DECIMAL(20,8) NOT NULL DEFAULT 0,
                                    transaction_type TINYINT NOT NULL,
                                    business_type VARCHAR(50) NOT NULL,
                                    business_id VARCHAR(64) NOT NULL,
                                    status TINYINT NOT NULL DEFAULT 0,
                                    remark VARCHAR(500),
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建余额变更历史表
CREATE TABLE balance_change_history (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        wallet_id BIGINT NOT NULL,
                                        transaction_id BIGINT NOT NULL,
                                        change_amount DECIMAL(20,8) NOT NULL,
                                        balance_before DECIMAL(20,8) NOT NULL,
                                        balance_after DECIMAL(20,8) NOT NULL,
                                        change_type VARCHAR(20) NOT NULL,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引（H2语法）
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_currency ON wallet(user_id, currency);
CREATE UNIQUE INDEX IF NOT EXISTS uk_transaction_no ON wallet_transaction(transaction_no);
CREATE UNIQUE INDEX IF NOT EXISTS uk_business ON wallet_transaction(business_type, business_id);
CREATE INDEX IF NOT EXISTS idx_user_id ON wallet(user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_id ON wallet_transaction(wallet_id);
CREATE INDEX IF NOT EXISTS idx_created ON wallet_transaction(created_at);