package com.bookkeeping.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 滑动窗口限流Lua脚本
     * KEYS[1]: 限流key
     * ARGV[1]: 窗口大小（毫秒）
     * ARGV[2]: 最大请求数
     * ARGV[3]: 当前时间戳（毫秒）
     */
    private static final String SLIDING_WINDOW_SCRIPT =
            "local key = KEYS[1]\n" +
            "local window = tonumber(ARGV[1])\n" +
            "local limit = tonumber(ARGV[2])\n" +
            "local now = tonumber(ARGV[3])\n" +
            "local window_start = now - window\n" +
            "redis.call('ZREMRANGEBYSCORE', key, 0, window_start)\n" +
            "local count = redis.call('ZCARD', key)\n" +
            "if count < limit then\n" +
            "    redis.call('ZADD', key, now, now)\n" +
            "    redis.call('PEXPIRE', key, window)\n" +
            "    return 1\n" +
            "else\n" +
            "    return 0\n" +
            "end";

    /**
     * 缓存基本的对象
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象并设置过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存的对象
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存的对象
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断key是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 构建token缓存key
     */
    public String buildTokenKey(Long userId) {
        return "token:user:" + userId;
    }

    /**
     * 缓存用户token
     */
    public void cacheUserToken(Long userId, String token, long timeout, TimeUnit unit) {
        String key = buildTokenKey(userId);
        set(key, token, timeout, unit);
    }

    /**
     * 获取用户token
     */
    public String getUserToken(Long userId) {
        String key = buildTokenKey(userId);
        Object value = get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 删除用户token
     */
    public void deleteUserToken(Long userId) {
        String key = buildTokenKey(userId);
        delete(key);
    }

    /**
     * 滑动窗口限流
     * @param key 限流key
     * @param windowMs 窗口大小（毫秒）
     * @param limit 最大请求数
     * @return 是否允许通过
     */
    public boolean tryAcquire(String key, long windowMs, int limit) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(SLIDING_WINDOW_SCRIPT);
            script.setResultType(Long.class);

            long now = System.currentTimeMillis();
            Long result = redisTemplate.execute(
                    script,
                    Collections.singletonList(key),
                    windowMs, limit, now
            );

            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Redis限流失败，降级放行: {}", e.getMessage());
            return true;
        }
    }
}
