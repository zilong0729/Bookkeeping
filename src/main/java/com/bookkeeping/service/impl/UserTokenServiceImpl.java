package com.bookkeeping.service.impl;

import com.bookkeeping.entity.UserToken;
import com.bookkeeping.mapper.UserTokenMapper;
import com.bookkeeping.service.UserTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户Token服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserTokenServiceImpl implements UserTokenService {

    private final UserTokenMapper userTokenMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserToken createOrUpdateToken(Long userId, String token, Integer deviceType,
                                         String deviceName, String deviceId,
                                         String ipAddress, String userAgent,
                                         Integer expireDays) {
        // 先查找同一设备的已有token
        UserToken existingToken = userTokenMapper.selectByDeviceId(userId, deviceId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusDays(expireDays);

        if (existingToken != null) {
            // 更新现有token记录
            existingToken.setToken(token);
            existingToken.setDeviceType(deviceType);
            existingToken.setDeviceName(deviceName);
            existingToken.setIpAddress(ipAddress);
            existingToken.setUserAgent(userAgent);
            existingToken.setLoginTime(now);
            existingToken.setExpireTime(expireTime);
            existingToken.setLastActiveTime(now);
            existingToken.setStatus(1);
            existingToken.setLoginCount(existingToken.getLoginCount() + 1);
            existingToken.setUpdateTime(now);
            userTokenMapper.updateById(existingToken);
            log.info("更新token记录: userId={}, deviceId={}", userId, deviceId);
            return existingToken;
        } else {
            // 创建新的token记录
            UserToken newToken = new UserToken();
            newToken.setUserId(userId);
            newToken.setToken(token);
            newToken.setDeviceType(deviceType);
            newToken.setDeviceName(deviceName);
            newToken.setDeviceId(deviceId);
            newToken.setIpAddress(ipAddress);
            newToken.setUserAgent(userAgent);
            newToken.setLoginTime(now);
            newToken.setExpireTime(expireTime);
            newToken.setLastActiveTime(now);
            newToken.setStatus(1);
            newToken.setLoginCount(1);
            newToken.setCreateTime(now);
            newToken.setUpdateTime(now);
            userTokenMapper.insert(newToken);
            log.info("创建新token记录: userId={}, deviceId={}", userId, deviceId);
            return newToken;
        }
    }

    @Override
    public UserToken findByToken(String token) {
        return userTokenMapper.selectByToken(token);
    }

    @Override
    public List<UserToken> findValidByUserId(Long userId) {
        return userTokenMapper.selectValidByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidateToken(String token) {
        UserToken userToken = userTokenMapper.selectByToken(token);
        if (userToken != null) {
            userToken.setStatus(0);
            userToken.setUpdateTime(LocalDateTime.now());
            userTokenMapper.updateById(userToken);
            log.info("使token失效: token={}", token);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidateAllByUserId(Long userId) {
        userTokenMapper.invalidateByUserId(userId);
        log.info("使用户所有token失效: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastActiveTime(Long tokenId) {
        userTokenMapper.updateLastActiveTime(tokenId, LocalDateTime.now());
    }

    @Override
    public Integer countLogin(Long userId) {
        return userTokenMapper.countLoginByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredTokens() {
        // 这里可以使用MyBatis-Plus的条件删除
        // 实际实现时可以添加一个定时任务来清理过期token
        log.info("清理过期token");
    }
}
