package com.bookkeeping.service;

import com.bookkeeping.vo.UserVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 统一管理Redis缓存操作
 */
public interface CacheService {

    // ==================== 用户缓存 ====================
    void cacheUser(Long userId, UserVO userVO);
    UserVO getUser(Long userId);
    void evictUser(Long userId);

    // ==================== 账单统计缓存 ====================
    void cacheStatistics(Long userId, Integer type, LocalDate startDate, LocalDate endDate, BigDecimal amount);
    BigDecimal getStatistics(Long userId, Integer type, LocalDate startDate, LocalDate endDate);
    void evictStatistics(Long userId);

    // ==================== 通用缓存 ====================
    void set(String key, Object value, long timeout, TimeUnit unit);
    Object get(String key);
    void delete(String key);
    void deleteByPattern(String pattern);
}
