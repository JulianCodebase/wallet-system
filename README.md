# 钱包系统设计与实现报告

## **1. 系统架构设计**

### **1.1 整体架构**
钱包系统采用分层架构设计，包含：

- **Controller层**：接收HTTP请求，参数校验，返回统一格式响应
- **Service层**：核心业务逻辑处理，事务管理
- **Mapper层**：数据持久化操作
- **数据库曾**：数据存储，索引优化

### **1.2 核心特性**

- 多币种钱包支持
- 交易幂等性保障
- 余额变更历史追溯
- 实时对账能力
- 防双花机制

## **2. 数据库设计**

### **2.1 核心表结构**

### **wallet（钱包表）**

```sql
CREATE TABLE wallet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    currency VARCHAR(10) NOT NULL COMMENT '币种',
    balance DECIMAL(20,8) NOT NULL DEFAULT 0 COMMENT '余额',
    frozen_balance DECIMAL(20,8) NOT NULL DEFAULT 0 COMMENT '冻结余额',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号，用于乐观锁',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    UNIQUE KEY uk_user_currency (user_id, currency)
)
```

### **wallet_transaction（交易流水表）**

```sql
CREATE TABLE wallet_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_no VARCHAR(64) NOT NULL COMMENT '交易流水号',
    wallet_id BIGINT NOT NULL COMMENT '钱包ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    currency VARCHAR(10) NOT NULL COMMENT '币种',
    amount DECIMAL(20,8) NOT NULL COMMENT '交易金额',
    balance_before DECIMAL(20,8) NOT NULL DEFAULT 0,
    balance_after DECIMAL(20,8) NOT NULL DEFAULT 0,
    transaction_type TINYINT NOT NULL COMMENT '交易类型',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    business_id VARCHAR(64) NOT NULL COMMENT '业务ID，用于幂等',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态',
    UNIQUE KEY uk_transaction_no (transaction_no),
    UNIQUE KEY uk_business (business_type, business_id)
)
```

### **balance_change_history（余额变更历史表）**

```sql
CREATE TABLE balance_change_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wallet_id BIGINT NOT NULL COMMENT '钱包ID',
    transaction_id BIGINT NOT NULL COMMENT '交易ID',
    change_amount DECIMAL(20,8) NOT NULL COMMENT '变更金额',
    balance_before DECIMAL(20,8) NOT NULL,
    balance_after DECIMAL(20,8) NOT NULL,
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
)
```

### **2.2 设计亮点**

- **唯一索引**：防止用户重复币种钱包、重复交易
- **乐观锁**：通过version字段防止并发更新
- **余额追踪**：完整记录每次余额变更轨迹
- **业务幂等**：通过business_type+business_id保证幂等性

## **3. 核心业务逻辑实现**

### **3.1 充值流程**

```java
public TransactionResponse recharge(Long userId, String currency, BigDecimal amount,  businessType, String businessId, String remark) {
	// 1. 幂等检查
	// 2. 获取或创建钱包
	// 3. 创建交易记录（处理中状态）
	// 4. 更新余额（原子操作）
	// 5. 更新交易状态为成功
		// 6. 记录余额变更历史
}
```

### **3.2 提现流程（防双花核心）**

```java
public TransactionResponse withdraw(Long userId, String currency, BigDecimal amount, String businessType, String businessId, String remark) {
	// 1. 幂等检查
	// 2. 余额充足性校验
	// 3. 创建交易记录
	// 4. 乐观锁扣减余额（防止超扣）
	// 5. 更新交易状态
	// 6. 记录变更历史
}
```

### **3.3 对账机制**

```java
public ReconciliationResult reconcile(Long userId, String currency, Date startTime, Date endTime) {
	// 1. 获取期初余额
	// 2. 查询期间所有变更记录
	// 3. 计算理论期末余额 = 期初余额 + 变更金额
	// 4. 获取实际钱包余额
	// 5. 比对理论值与实际值
}
```

## **4. 安全保障机制**

### **4.1 防双花设计**

- **乐观锁机制**：`update wallet set balance = balance - ?, version = version + 1 where id = ? and version = ?`
- **余额前置检查**：在事务内再次验证余额充足性
- **事务隔离**：保证余额查询和更新的原子性

### **4.2 防重复入账**

- **业务唯一键**：`uk_business (business_type, business_id)` 唯一索引
- **幂等检查**：在业务开始时检查是否已处理过相同业务请求
- **事务回滚**：异常时自动回滚，保证数据一致性

### **4.3 数据一致性保障**

- **事务管理**：`@Transactional(rollbackFor = Exception.class)`
- **余额追踪**：每次变更都记录完整的历史轨迹
- **状态机管理**：交易状态从处理中→成功/失败，避免中间状态

## **5. 性能分析与优化**

### **5.1 吞吐量分析**

**预期性能指标**：

- 单节点TPS：500-1000笔/秒（写操作）
- QPS：2000-5000次/秒（读操作）
- 响应时间：<100ms（95%请求）

### **5.2 潜在瓶颈点**

### **数据库层面**

1. **钱包表热点更新**：高频用户的钱包记录更新冲突
2. **交易表写入压力**：所有交易都要记录，写入量巨大
3. **余额变更历史表**：数据增长快，查询性能下降

### **应用层面**

1. **数据库连接池**：高并发下连接数不足
2. **事务持有时间**：长事务导致锁竞争
3. **内存使用**：大查询导致内存溢出

### **5.3 优化方案**

### **5.3.1 数据库优化**

```sql
-- 分表策略-- 按用户ID分表：wallet_${user_id % 64}-- 按时间分表：transaction_202401, transaction_202402-- 索引优化ALTER TABLE wallet_transaction ADD INDEX idx_user_currency_time (user_id, currency, created_at);
ALTER TABLE balance_change_history ADD INDEX idx_wallet_time (wallet_id, created_at);
```

### **5.3.2 缓存策略**

```java
// 1. 钱包余额缓存@Cacheable(value = "walletBalance", key = "#userId + '_' + #currency")
public CurrencyBalance getCurrencyBalance(Long userId, String currency) {
	// 查询数据库
}

// 2. 交易结果缓存（短期）@Cacheable(value = "transactionResult", key = "#businessType + '_' + #businessId")
public TransactionResponse getTransactionByBusiness(String businessType, String businessId) {
	// 查询数据库
}
```

### **5.3.3 异步处理**

```java
// 异步记录余额变更历史@Async
public void asyncRecordBalanceChange(WalletTransaction transaction, Wallet wallet, BigDecimal balanceAfter) {
    recordBalanceChange(transaction, wallet, balanceAfter);
}
```

### **5.3.4 批量操作**

```java
// 批量插入余额变更历史
public void batchInsertBalanceHistory(List<BalanceChangeHistory> histories) {
    balanceChangeHistoryMapper.batchInsert(histories);
}
```

## **6. 扩展性设计**

### **6.1 水平扩展**

- **应用层无状态**：支持多实例部署
- **数据库分库分表**：按用户ID进行数据分片
- **读写分离**：交易写主库，查询读从库

### **6.2 功能扩展**

- **多级钱包**：支持主钱包、子钱包体系
- **资金冻结**：预冻结机制支持业务预留
- **手续费计算**：灵活的手续费规则引擎
- **风控集成**：实时风控检查拦截可疑交易

## **7. 监控与运维**

### **7.1 关键监控指标**

- 交易成功率、失败率
- 系统响应时间
- 数据库连接池使用率
- 慢查询统计
- 余额不一致告警

### **7.2 对账保障**

- **每日定时对账**：自动检测余额不一致
- **实时对账接口**：支持业务方主动对账
- **差异自动修复**：小金额差异自动调平

## **8. 总结**

本钱包系统设计具备以下特点：

1. **安全性**：通过乐观锁、幂等设计、事务管理保障资金安全
2. **可追溯**：完整的余额变更历史，支持精确对账
3. **高性能**：合理的索引设计、缓存策略支撑高并发
4. **可扩展**：分层架构、分库分表支持业务增长
5. **易维护**：清晰的代码结构、完善的监控体系

系统在保障资金安全的前提下，通过多种优化手段提升了处理性能，能够满足大多数互联网业务的钱包需求。随着业务量的增长，可以通过水平扩展和进一步的性能优化来支撑更高的并发量