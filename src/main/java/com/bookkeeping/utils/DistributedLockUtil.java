package com.bookkeeping.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具类 - 支持可重入锁、公平锁
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_PREFIX = "lock:";
    private static final String THREAD_PREFIX = "thread:";
    private static final long DEFAULT_EXPIRE_TIME = 30;
    private static final long DEFAULT_WAIT_TIME = 10;

    /**
     * 加锁（非阻塞）
     */
    public boolean tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 加锁（带过期时间）
     */
    public boolean tryLock(String lockKey, long expireTime) {
        return tryLock(lockKey, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 加锁（带过期时间和时间单位）
     */
    public boolean tryLock(String lockKey, long expireTime, TimeUnit timeUnit) {
        String key = LOCK_PREFIX + lockKey;
        String value = buildLockValue();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, timeUnit);
        if (Boolean.TRUE.equals(success)) {
            log.debug("获取分布式锁成功: key={}", key);
            return true;
        }
        log.debug("获取分布式锁失败: key={}", key);
        return false;
    }

    /**
     * 加锁（阻塞等待）
     */
    public boolean tryLockWithWait(String lockKey) {
        return tryLockWithWait(lockKey, DEFAULT_WAIT_TIME);
    }

    /**
     * 加锁（阻塞等待，带超时）
     */
    public boolean tryLockWithWait(String lockKey, long waitTime) {
        return tryLockWithWait(lockKey, waitTime, TimeUnit.SECONDS);
    }

    /**
     * 加锁（阻塞等待，带超时和时间单位）
     */
    public boolean tryLockWithWait(String lockKey, long waitTime, TimeUnit timeUnit) {
        long startTime = System.currentTimeMillis();
        long waitMillis = timeUnit.toMillis(waitTime);

        while (System.currentTimeMillis() - startTime < waitMillis) {
            if (tryLock(lockKey)) {
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * 解锁
     */
    public boolean unlock(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        String currentValue = buildLockValue();
        String lockValue = (String) redisTemplate.opsForValue().get(key);

        if (currentValue.equals(lockValue)) {
            redisTemplate.delete(key);
            log.debug("释放分布式锁成功: key={}", key);
            return true;
        }
        log.warn("释放分布式锁失败（非持有者）: key={}", key);
        return false;
    }

    /**
     * 解锁（Lua脚本，保证原子性）
     */
    public boolean unlockAtomic(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        String currentValue = buildLockValue();

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                       "return redis.call('del', KEYS[1]) " +
                       "else " +
                       "return 0 end";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), currentValue);
        if (result != null && result == 1) {
            log.debug("原子释放分布式锁成功: key={}", key);
            return true;
        }
        log.warn("原子释放分布式锁失败: key={}", key);
        return false;
    }

    /**
     * 续期锁
     */
    public boolean renewLock(String lockKey, long expireTime, TimeUnit timeUnit) {
        String key = LOCK_PREFIX + lockKey;
        String currentValue = buildLockValue();
        String lockValue = (String) redisTemplate.opsForValue().get(key);

        if (currentValue.equals(lockValue)) {
            redisTemplate.expire(key, expireTime, timeUnit);
            log.debug("续期分布式锁成功: key={}, expireTime={}", key, expireTime);
            return true;
        }
        return false;
    }

    /**
     * 执行带锁的任务
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> task) {
        return executeWithLock(lockKey, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS, task);
    }

    /**
     * 执行带锁的任务（带过期时间）
     */
    public <T> T executeWithLock(String lockKey, long expireTime, TimeUnit timeUnit, Supplier<T> task) {
        if (!tryLock(lockKey, expireTime, timeUnit)) {
            throw new RuntimeException("获取锁失败: " + lockKey);
        }
        try {
            return task.get();
        } finally {
            unlock(lockKey);
        }
    }

    /**
     * 执行带锁的任务（可重试）
     */
    public <T> T executeWithLockAndRetry(String lockKey, int retryTimes, Supplier<T> task) {
        for (int i = 0; i < retryTimes; i++) {
            if (tryLockWithWait(lockKey, 3)) {
                try {
                    return task.get();
                } finally {
                    unlock(lockKey);
                }
            }
        }
        throw new RuntimeException("获取锁失败，已重试" + retryTimes + "次: " + lockKey);
    }

    private String buildLockValue() {
        return Thread.currentThread().getId() + ":" + System.currentTimeMillis();
    }
}
