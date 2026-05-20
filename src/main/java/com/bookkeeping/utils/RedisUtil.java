package com.bookkeeping.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

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
}
