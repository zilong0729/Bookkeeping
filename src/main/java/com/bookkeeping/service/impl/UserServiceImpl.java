package com.bookkeeping.service.impl;

import com.bookkeeping.dto.LoginRequestDTO;
import com.bookkeeping.dto.UpdateUserDTO;
import com.bookkeeping.dto.WxLoginDTO;
import com.bookkeeping.entity.User;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.UserMapper;
import com.bookkeeping.service.UserService;
import com.bookkeeping.service.UserTokenService;
import com.bookkeeping.utils.JwtUtil;
import com.bookkeeping.utils.RedisUtil;
import com.bookkeeping.vo.LoginVO;
import com.bookkeeping.vo.UserVO;
import com.bookkeeping.vo.WxSessionVO;
import com.bookkeeping.config.WxMiniAppConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final WxMiniAppConfig wxMiniAppConfig;
    private final UserTokenService userTokenService;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO wxLogin(WxLoginDTO loginDTO) throws JsonProcessingException {
        // 调用带设备信息的登录方法，设备信息为空
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setCode(loginDTO.getCode());
        requestDTO.setNickname(loginDTO.getNickname());
        requestDTO.setAvatarUrl(loginDTO.getAvatarUrl());
        requestDTO.setDeviceType(1); // 默认微信小程序
        return wxLoginWithDevice(requestDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO wxLoginWithDevice(LoginRequestDTO loginRequest) throws JsonProcessingException {
        // 1. 调用微信接口获取openid和session_key
        String url = wxMiniAppConfig.getAuthUrl(loginRequest.getCode());
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new BusinessException("微信登录失败，请稍后重试");
        }

        WxSessionVO sessionVO = objectMapper.readValue(response.getBody(), WxSessionVO.class);

        if (sessionVO.getErrcode() != null && sessionVO.getErrcode() != 0) {
            log.error("微信登录失败: {}", sessionVO.getErrmsg());
            throw new BusinessException("微信登录失败: " + sessionVO.getErrmsg());
        }

        String openid = sessionVO.getOpenid();
        if (openid == null || openid.isEmpty()) {
            throw new BusinessException("获取微信用户信息失败");
        }

        // 2. 根据openid查询或创建用户
        User user = userMapper.selectByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(sessionVO.getUnionid());
            user.setNickname(loginRequest.getNickname());
            user.setAvatarUrl(loginRequest.getAvatarUrl());
            user.setStatus(1);
            userMapper.insert(user);
            log.info("创建新用户: userId={}, openid={}", user.getId(), openid);
        } else {
            if (loginRequest.getNickname() != null && !loginRequest.getNickname().isEmpty()) {
                user.setNickname(loginRequest.getNickname());
            }
            if (loginRequest.getAvatarUrl() != null && !loginRequest.getAvatarUrl().isEmpty()) {
                user.setAvatarUrl(loginRequest.getAvatarUrl());
            }
            userMapper.updateById(user);
        }

        // 3. 生成token
        String token = jwtUtil.generateToken(user.getId(), openid, user.getRole());

        // 4. 存储token到数据库（使用UserTokenService）
        userTokenService.createOrUpdateToken(
                user.getId(),
                token,
                loginRequest.getDeviceType() != null ? loginRequest.getDeviceType() : 1,
                loginRequest.getDeviceName(),
                loginRequest.getDeviceId(),
                loginRequest.getIpAddress(),
                loginRequest.getUserAgent(),
                7 // 7天有效期
        );

        // 5. 缓存token到Redis
        redisUtil.cacheUserToken(user.getId(), token, 7, TimeUnit.DAYS);

        // 6. 组装返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setExpiresIn(7 * 24 * 60 * 60L);
        loginVO.setUserInfo(convertToUserVO(user));

        return loginVO;
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(Long userId, UpdateUserDTO updateDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDTO.getAvatarUrl());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }

        userMapper.updateById(user);
        return convertToUserVO(user);
    }

    @Override
    public void logout(Long userId) {
        // 从Redis获取token
        String token = redisUtil.getUserToken(userId);
        if (token != null) {
            // 使数据库中的token失效
            userTokenService.invalidateToken(token);
        }
        // 清除Redis中的token
        redisUtil.deleteUserToken(userId);
        log.info("用户登出: userId={}", userId);
    }

    @Override
    public void forceLogout(Long userId) {
        // 使用户所有token失效
        userTokenService.invalidateAllByUserId(userId);
        // 清除Redis中的token
        redisUtil.deleteUserToken(userId);
        log.info("强制用户下线: userId={}", userId);
    }

    /**
     * 转换为UserVO
     */
    private UserVO convertToUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setRole(user.getRole());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
