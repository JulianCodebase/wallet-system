-- 创建数据库
CREATE DATABASE IF NOT EXISTS wallet_db DEFAULT CHARSET utf8mb4;

USE wallet_db;

-- 钱包表
CREATE TABLE wallet (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        user_id BIGINT NOT NULL COMMENT '用户ID',
                        currency VARCHAR(10) NOT NULL COMMENT '币种: USD,CNY,BTC,ETH',
                        balance DECIMAL(20,8) NOT NULL DEFAULT 0 COMMENT '余额',
                        frozen_balance DECIMAL(20,8) NOT NULL DEFAULT 0 COMMENT '冻结余额',
                        version INT NOT NULL DEFAULT 0 COMMENT '版本号，用于乐观锁',
                        status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1正常,0冻结',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        UNIQUE KEY uk_user_currency (user_id, currency),
                        KEY idx_user_id (user_id),
                        KEY idx_currency (currency)
) ENGINE=InnoDB COMMENT='钱包表';

-- 交易流水表
CREATE TABLE wallet_transaction (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    transaction_no VARCHAR(64) NOT NULL COMMENT '交易流水号，全局唯一',
                                    wallet_id BIGINT NOT NULL COMMENT '钱包ID',
                                    user_id BIGINT NOT NULL COMMENT '用户ID',
                                    currency VARCHAR(10) NOT NULL COMMENT '币种',
                                    amount DECIMAL(20,8) NOT NULL COMMENT '交易金额，正数表示收入，负数表示支出',
                                    balance_before DECIMAL(20,8) NOT NULL DEFAULT 0 COMMENT '交易前余额',
                                    balance_after DECIMAL(20,8) NOT NULL DEFAULT 0 COMMENT '交易后余额',
                                    transaction_type TINYINT NOT NULL COMMENT '交易类型:1充值,2提现,3转账,4消费',
                                    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
                                    business_id VARCHAR(64) NOT NULL COMMENT '业务ID，用于幂等',
                                    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0处理中,1成功,2失败',
                                    remark VARCHAR(500) COMMENT '备注',
                                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    UNIQUE KEY uk_transaction_no (transaction_no),
                                    UNIQUE KEY uk_business (business_type, business_id),
                                    KEY idx_wallet_id (wallet_id),
                                    KEY idx_user_id (user_id),
                                    KEY idx_created (created_at)
) ENGINE=InnoDB COMMENT='钱包交易流水表';

-- 余额变更历史表
CREATE TABLE balance_change_history (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        wallet_id BIGINT NOT NULL COMMENT '钱包ID',
                                        transaction_id BIGINT NOT NULL COMMENT '交易ID',
                                        change_amount DECIMAL(20,8) NOT NULL COMMENT '变更金额',
                                        balance_before DECIMAL(20,8) NOT NULL COMMENT '变更前余额',
                                        balance_after DECIMAL(20,8) NOT NULL COMMENT '变更后余额',
                                        change_type VARCHAR(20) NOT NULL COMMENT '变更类型:BALANCE,FROZEN',
                                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                        KEY idx_wallet_id (wallet_id),
                                        KEY idx_transaction_id (transaction_id),
                                        KEY idx_created (created_at)
) ENGINE=InnoDB COMMENT='余额变更历史表';