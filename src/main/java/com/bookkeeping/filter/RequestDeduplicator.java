package com.bookkeeping.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 请求去重工具 - 防止微信小程序重复提交
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestDeduplicator {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String DEDUP_PREFIX = "dedup:";

    /**
     * 生成请求ID
     */
    public String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 尝试加锁（用于创建操作）
     * @param userId 用户ID
     * @param operation 操作类型
     * @param timeout 锁超时时间（秒）
     * @return 是否成功获取锁
     */
    public boolean tryLock(Long userId, String operation, int timeout) {
        String key = buildKey(userId, operation);
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", timeout, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            log.debug("获取去重锁成功: key={}", key);
            return true;
        }
        log.debug("获取去重锁失败（操作进行中）: key={}", key);
        return false;
    }

    /**
     * 释放锁
     */
    public void unlock(Long userId, String operation) {
        String key = buildKey(userId, operation);
        redisTemplate.delete(key);
    }

    /**
     * 检查是否是重复请求
     * @param requestId 请求ID
     * @param timeout 有效期（秒）
     * @return true-重复请求，false-新请求
     */
    public boolean isDuplicate(String requestId, int timeout) {
        if (requestId == null || requestId.isEmpty()) {
            return false;
        }
        String key = DEDUP_PREFIX + requestId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", timeout, TimeUnit.SECONDS);
        return !Boolean.TRUE.equals(success);
    }

    /**
     * 标记请求已处理（用于异步处理场景）
     */
    public void markProcessed(String requestId, int expireSeconds) {
        String key = DEDUP_PREFIX + requestId;
        redisTemplate.opsForValue().set(key, "processed", expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 检查请求是否已处理
     */
    public boolean isProcessed(String requestId) {
        String key = DEDUP_PREFIX + requestId;
        Object value = redisTemplate.opsForValue().get(key);
        return "processed".equals(value);
    }

    private String buildKey(Long userId, String operation) {
        return DEDUP_PREFIX + userId + ":" + operation;
    }
}
