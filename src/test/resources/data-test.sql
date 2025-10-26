-- 清理测试数据
DELETE
FROM balance_change_history;
DELETE
FROM wallet_transaction;
DELETE
FROM wallet;

-- 插入测试钱包数据
INSERT INTO wallet (user_id, currency, balance, frozen_balance, version, status)
VALUES (1001, 'CNY', 1000.00, 0.00, 0, 1),
       (1001, 'USD', 100.00, 0.00, 0, 1),
       (1002, 'CNY', 500.00, 0.00, 0, 1),
       (1003, 'CNY', 0.00, 0.00, 0, 1);