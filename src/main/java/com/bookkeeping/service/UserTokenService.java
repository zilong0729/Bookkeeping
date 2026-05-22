package com.bookkeeping.service;

import com.bookkeeping.entity.UserToken;

import java.util.List;

/**
 * 用户Token服务接口
 */
public interface UserTokenService {

    /**
     * 创建或更新token记录
     */
    UserToken createOrUpdateToken(Long userId, String token, Integer deviceType, 
                                   String deviceName, String deviceId, 
                                   String ipAddress, String userAgent, 
                                   Integer expireDays);

    /**
     * 根据token查询
     */
    UserToken findByToken(String token);

    /**
     * 根据用户ID查询所有有效token
     */
    List<UserToken> findValidByUserId(Long userId);

    /**
     * 使指定token失效
     */
    void invalidateToken(String token);

    /**
     * 使用户所有token失效（强制下线）
     */
    void invalidateAllByUserId(Long userId);

    /**
     * 更新最后活跃时间
     */
    void updateLastActiveTime(Long tokenId);

    /**
     * 统计用户登录次数
     */
    Integer countLogin(Long userId);

    /**
     * 清理过期token
     */
    void cleanExpiredTokens();
}
