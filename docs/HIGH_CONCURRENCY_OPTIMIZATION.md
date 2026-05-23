# 微信小程序高并发优化指南

## 📋 目录
1. [高并发场景分析](#1-高并发场景分析)
2. [已实施的优化措施](#2-已实施的优化措施)
3. [使用指南](#3-使用指南)
4. [监控与告警](#4-监控与告警)
5. [常见问题处理](#5-常见问题处理)

---

## 1. 高并发场景分析

### 🎯 微信小程序特有场景

| 场景 | 描述 | 潜在问题 |
|------|------|----------|
| **快速点击** | 用户双击或快速点击按钮 | 重复提交订单、重复创建记录 |
| **弱网环境** | 网络不稳定、请求超时 | 请求重试导致重复操作 |
| **多设备登录** | 用户在多个设备登录 | 数据同步冲突 |
| **活动高峰期** | 节假日、特殊日期 | 系统过载、数据库连接耗尽 |
| **分享功能** | 用户互相邀请 | 并发创建好友关系 |

### ⚠️ 已识别的高并发问题

1. **幂等性问题** - 缺少请求去重机制
2. **并发更新问题** - 更新操作可能被覆盖
3. **缓存击穿** - 热数据过期瞬间大量请求打到数据库
4. **分布式锁缺失** - 关键操作无锁保护
5. **限流粒度不够** - 只有登录接口限流
6. **连接池不足** - 高并发时连接耗尽

---

## 2. 已实施的优化措施

### ✅ 2.1 幂等性控制

**新增文件**: `RequestDeduplicator.java`

**功能**:
- 请求去重，防止重复提交
- 支持基于用户+操作的分布式锁
- 请求ID追踪

**使用示例**:
```java
@Autowired
private RequestDeduplicator deduplicator;

@PostMapping("/create")
public Result<Void> create(@RequestBody Request req) {
    // 尝试获取锁
    if (!deduplicator.tryLock(userId, "create_record", 10)) {
        throw new BusinessException("操作进行中，请勿重复提交");
    }
    try {
        // 业务逻辑
        recordService.create(userId, req);
    } finally {
        deduplicator.unlock(userId, "create_record");
    }
}
```

### ✅ 2.2 分布式锁

**新增文件**: `DistributedLockUtil.java`

**功能**:
- 可重入分布式锁
- 公平锁支持
- 锁续期机制
- 原子解锁（Lua脚本）

**使用示例**:
```java
@Autowired
private DistributedLockUtil lockUtil;

public void createFriendRelation(Long userId, Long friendId) {
    String lockKey = "friend:" + userId + ":" + friendId;

    if (lockUtil.executeWithLock(lockKey, () -> {
        // 业务逻辑 - 保证原子性执行
        doCreateFriendRelation(userId, friendId);
    })) {
        // 成功
    } else {
        throw new BusinessException("系统繁忙，请重试");
    }
}
```

### ✅ 2.3 多维度限流

**更新文件**:
- `RateLimit.java` - 支持多种限流类型
- `RateLimitAspect.java` - 增强版限流切面
- `RecordController.java` - 添加限流注解

**限流类型**:
```java
@RateLimit(limitType = LimitType.USER, permitsPerSecond = 5)
@RateLimit(limitType = LimitType.IP, permitsPerSecond = 100)
@RateLimit(limitType = LimitType.USER_INTERFACE, permitsPerSecond = 10)
```

**推荐限流配置**:

| 接口 | 限流类型 | QPS | 说明 |
|------|----------|-----|------|
| 创建账单 | USER | 5 | 防止重复提交 |
| 更新账单 | USER | 10 | 正常操作频率 |
| 删除账单 | USER | 10 | 正常操作频率 |
| 查询列表 | USER | 20 | 查询可稍高 |
| 查询统计 | USER | 30 | 统计查询 |

### ✅ 2.4 数据库连接池优化

**新增文件**: `DatabaseConfig.java`

**优化内容**:
- 连接池大小: 20-200
- PreparedStatement缓存: 50
- 异步初始化
- 连接泄漏检测

**生产环境配置**:
```yaml
spring:
  datasource:
    druid:
      initial-size: 20
      min-idle: 20
      max-active: 200
      max-wait: 60000
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 50
```

### ✅ 2.5 分享服务并发控制

**更新文件**: `ShareServiceImpl.java`

**优化内容**:
- 接受分享时添加分布式锁
- 好友关系创建添加并发控制
- 分享码唯一性检查
- 联系人去重

```java
// 接受分享加锁
String lockKey = "accept_share:" + shareCode + ":" + userId;
if (!lockUtil.tryLockWithWait(lockKey, 5)) {
    throw new BusinessException("操作进行中，请稍后重试");
}

// 好友关系创建加锁
String friendLockKey = "create_friend:" + userId + ":" + friendUserId;
if (!lockUtil.tryLock(friendLockKey, 10)) {
    return; // 或抛出异常
}
```

### ✅ 2.6 服务器配置优化

**生产环境配置** (`application-prod.yml`):
```yaml
server:
  tomcat:
    threads:
      max: 500        # 最大线程数
      min-spare: 50   # 最小空闲线程
    max-connections: 10000
    accept-count: 500

spring:
  task:
    execution:
      pool:
        core-size: 20
        max-size: 100
        queue-capacity: 500

  redis:
    lettuce:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 10
```

### ✅ 2.7 异步任务执行器

**新增文件**: `AsyncTaskExecutor.java`

**功能**:
- 异步执行
- 自动重试
- 可配置延迟

```java
@Autowired
private AsyncTaskExecutor asyncTaskExecutor;

// 异步发送通知（带重试）
asyncTaskExecutor.executeWithRetry(() -> {
    wechatPushService.sendNotification(userId, message);
}, 3, 1000); // 最多重试3次，每次延迟1秒
```

### ✅ 2.8 系统健康检查

**新增文件**:
- `HealthCheck.java` - 健康检查组件
- `MonitorController.java` - 监控接口

**访问地址**: `GET /monitor/health`

**返回示例**:
```json
{
  "status": "UP",
  "database": {
    "status": "UP",
    "activeCount": 15,
    "poolingCount": 25,
    "maxActive": 200
  },
  "redis": {
    "status": "UP"
  },
  "threadPool": {
    "status": "UP",
    "availableProcessors": 8,
    "memoryUsagePercent": "65.32"
  }
}
```

---

## 3. 使用指南

### 3.1 启动生产环境

```bash
# Linux/Mac
chmod +x scripts/start-prod.sh
./scripts/start-prod.sh

# Windows
# 双击运行 start-prod.bat 或使用命令行
```

### 3.2 JVM参数配置

编辑 `scripts/start-prod.sh` 中的JVM参数：

```bash
# 堆内存设置（根据服务器内存调整）
HEAP_SIZE="4g"  # 建议为可用内存的50-75%

# Metaspace大小
METASPACE_SIZE="512m"
```

**推荐配置**:

| 服务器内存 | 堆大小 | 最大线程 | 连接池 |
|-----------|--------|----------|--------|
| 4GB | 2GB | 300 | 100 |
| 8GB | 4GB | 500 | 200 |
| 16GB | 8GB | 800 | 300 |

### 3.3 配置说明

#### 环境变量配置

在生产环境使用环境变量或 `.env` 文件：

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=bookkeeping
export DB_USERNAME=root
export DB_PASSWORD=your-secure-password

export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=

export ADMIN_INIT_USERNAME=admin
export ADMIN_INIT_PASSWORD=your-secure-admin-password
```

---

## 4. 监控与告警

### 4.1 健康检查接口

```bash
# 检查系统健康状态
curl http://localhost:8080/bookkeeping-server/monitor/health

# 检查数据库连接
curl http://localhost:8080/bookkeeping-server/druid/datasource.html
```

### 4.2 日志分析

```bash
# 查看应用日志
tail -f logs/bookkeeping-server.log

# 查看GC日志
tail -f logs/gc.log

# 查看错误日志
grep -i error logs/bookkeeping-server.log
```

### 4.3 关键指标监控

| 指标 | 告警阈值 | 说明 |
|------|----------|------|
| 数据库连接数 | > 180 | 连接池使用率 > 90% |
| 响应时间 | > 3s | 用户体验受影响 |
| 错误率 | > 5% | 系统异常 |
| 内存使用率 | > 85% | 可能OOM |

---

## 5. 常见问题处理

### 5.1 数据库连接耗尽

**症状**: 报错 "Cannot get a connection, pool exhausted"

**解决方案**:
1. 检查是否有连接泄漏（执行时间过长的SQL）
2. 增加连接池大小
3. 添加数据库慢查询日志

```yaml
spring:
  datasource:
    druid:
      max-active: 300
      time-between-eviction-runs-millis: 60000
      remove-abandoned: true
      remove-abandoned-timeout: 60
```

### 5.2 Redis连接超时

**症状**: 报错 "Redis connection timeout"

**解决方案**:
1. 检查Redis服务状态
2. 增加Redis连接超时时间
3. 使用连接池

```yaml
spring:
  redis:
    timeout: 10000ms  # 增加超时时间
    lettuce:
      pool:
        max-active: 50
        max-wait: 5000ms
```

### 5.3 限流触发过于频繁

**症状**: 用户频繁收到限流提示

**解决方案**:
1. 分析热点接口
2. 适当提高限流阈值
3. 添加接口缓存

```java
@RateLimit(limitType = LimitType.USER, permitsPerSecond = 10)
```

### 5.4 分布式锁获取失败

**症状**: 报错 "获取锁失败"

**解决方案**:
1. 检查Redis服务
2. 调整锁超时时间
3. 增加重试次数

```java
// 增加重试
lockUtil.executeWithLockAndRetry(lockKey, 3, () -> {
    // 业务逻辑
});
```

### 5.5 OOM内存溢出

**症状**: 应用崩溃，生成heapdump.hprof

**解决方案**:
1. 增加堆内存
2. 分析heapdump找出内存泄漏
3. 优化JVM参数

```bash
# 使用 MAT 分析 heapdump
java -jar mat/HeapHero.jar logs/heapdump.hprof
```

---

## 📞 技术支持

如有问题，请查看：
- 应用日志: `logs/bookkeeping-server.log`
- GC日志: `logs/gc.log`
- 控制台输出: `logs/console.log`

---

**最后更新**: 2025-05-23
