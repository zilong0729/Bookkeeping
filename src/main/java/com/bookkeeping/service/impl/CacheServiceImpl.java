package com.bookkeeping.service.impl;

import com.bookkeeping.service.CacheService;
import com.bookkeeping.utils.RedisUtil;
import com.bookkeeping.vo.UserVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisUtil redisUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 缓存前缀
    private static final String USER_CACHE_PREFIX = "user:";
    private static final String STATISTICS_CACHE_PREFIX = "statistics:";

    // 缓存过期时间
    private static final long USER_CACHE_EXPIRE = 30; // 30分钟
    private static final long STATISTICS_CACHE_EXPIRE = 10; // 10分钟

    @Override
    public void cacheUser(Long userId, UserVO userVO) {
        try {
            String key = USER_CACHE_PREFIX + userId;
            redisUtil.set(key, userVO, USER_CACHE_EXPIRE, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存用户信息失败: userId={}", userId, e);
        }
    }

    @Override
    public UserVO getUser(Long userId) {
        try {
            String key = USER_CACHE_PREFIX + userId;
            Object value = redisUtil.get(key);
            if (value != null) {
                String jsonStr = objectMapper.writeValueAsString(value);
                return objectMapper.readValue(jsonStr, UserVO.class);
            }
        } catch (Exception e) {
            log.warn("获取用户缓存失败: userId={}", userId, e);
        }
        return null;
    }

    @Override
    public void evictUser(Long userId) {
        try {
            String key = USER_CACHE_PREFIX + userId;
            redisUtil.delete(key);
        } catch (Exception e) {
            log.warn("清除用户缓存失败: userId={}", userId, e);
        }
    }

    @Override
    public void cacheStatistics(Long userId, Integer type, LocalDate startDate, LocalDate endDate, BigDecimal amount) {
        try {
            String key = buildStatisticsKey(userId, type, startDate, endDate);
            redisUtil.set(key, amount, STATISTICS_CACHE_EXPIRE, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存统计信息失败: userId={}", userId, e);
        }
    }

    @Override
    public BigDecimal getStatistics(Long userId, Integer type, LocalDate startDate, LocalDate endDate) {
        try {
            String key = buildStatisticsKey(userId, type, startDate, endDate);
            Object value = redisUtil.get(key);
            if (value != null) {
                return new BigDecimal(value.toString());
            }
        } catch (Exception e) {
            log.warn("获取统计缓存失败: userId={}", userId, e);
        }
        return null;
    }

    @Override
    public void evictStatistics(Long userId) {
        try {
            String pattern = STATISTICS_CACHE_PREFIX + userId + ":*";
            deleteByPattern(pattern);
        } catch (Exception e) {
            log.warn("清除统计缓存失败: userId={}", userId, e);
        }
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisUtil.set(key, value, timeout, unit);
    }

    @Override
    public Object get(String key) {
        return redisUtil.get(key);
    }

    @Override
    public void delete(String key) {
        redisUtil.delete(key);
    }

    @Override
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("按模式删除缓存失败: pattern={}", pattern, e);
        }
    }

    private String buildStatisticsKey(Long userId, Integer type, LocalDate startDate, LocalDate endDate) {
        return STATISTICS_CACHE_PREFIX + userId + ":" + type + ":" + startDate + ":" + endDate;
    }
}
