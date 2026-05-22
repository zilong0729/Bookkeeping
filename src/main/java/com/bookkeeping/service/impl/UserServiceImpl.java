package com.bookkeeping.service.impl;

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
import com.bookkeeping.vo.req.LoginReqVO;
import com.bookkeeping.vo.req.UserUpdateReqVO;
import com.bookkeeping.vo.resp.LoginRespVO;
import com.bookkeeping.vo.resp.UserRespVO;
import com.bookkeeping.config.WxMiniAppConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

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
    public LoginRespVO wxLogin(LoginReqVO loginVO) throws JsonProcessingException {
        String url = wxMiniAppConfig.getAuthUrl(loginVO.getCode());
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

        User user = userMapper.selectByOpenid(openid);
        boolean firstLogin = false;
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(sessionVO.getUnionid());
            user.setStatus(1);
            userMapper.insert(user);
            firstLogin = true;
            log.info("创建新用户: userId={}, openid={}", user.getId(), openid);
        }

        String token = jwtUtil.generateToken(user.getId(), openid, user.getRole());

        userTokenService.createOrUpdateToken(
                user.getId(),
                token,
                loginVO.getDeviceType() != null ? loginVO.getDeviceType() : 1,
                null,
                loginVO.getDeviceId(),
                null,
                null,
                7
        );

        redisUtil.cacheUserToken(user.getId(), token, 7, TimeUnit.DAYS);

        return LoginRespVO.builder()
                .token(token)
                .expiresIn(7 * 24 * 60 * 60L)
                .userInfo(UserRespVO.builder().id(user.getId())
                        .nickname(user.getNickname())
                        .avatarUrl(user.getAvatarUrl())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .build())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO wxLoginWithDevice(LoginReqVO loginVO) throws JsonProcessingException {
        String url = wxMiniAppConfig.getAuthUrl(loginVO.getCode());
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

        User user = userMapper.selectByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(sessionVO.getUnionid());
            user.setStatus(1);
            userMapper.insert(user);
            log.info("创建新用户: userId={}, openid={}", user.getId(), openid);
        }

        String token = jwtUtil.generateToken(user.getId(), openid, user.getRole());

        userTokenService.createOrUpdateToken(
                user.getId(),
                token,
                loginVO.getDeviceType() != null ? loginVO.getDeviceType() : 1,
                null,
                loginVO.getDeviceId(),
                null,
                null,
                7
        );

        redisUtil.cacheUserToken(user.getId(), token, 7, TimeUnit.DAYS);

        LoginVO result = new LoginVO();
        result.setToken(token);
        result.setExpiresIn(7 * 24 * 60 * 60L);
        result.setUserInfo(convertToUserVO(user));

        return result;
    }

    @Override
    public UserRespVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToRespVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRespVO updateUser(Long userId, UserUpdateReqVO updateVO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (updateVO.getNickname() != null) {
            user.setNickname(updateVO.getNickname());
        }
        if (updateVO.getAvatarUrl() != null) {
            user.setAvatarUrl(updateVO.getAvatarUrl());
        }
        if (updateVO.getPhone() != null) {
            user.setPhone(updateVO.getPhone());
        }

        userMapper.updateById(user);
        return convertToRespVO(user);
    }

    @Override
    public void logout(Long userId) {
        String token = redisUtil.getUserToken(userId);
        if (token != null) {
            userTokenService.invalidateToken(token);
        }
        redisUtil.deleteUserToken(userId);
        log.info("用户登出: userId={}", userId);
    }

    @Override
    public void forceLogout(Long userId) {
        userTokenService.invalidateAllByUserId(userId);
        redisUtil.deleteUserToken(userId);
        log.info("强制用户下线: userId={}", userId);
    }

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

    private UserRespVO convertToRespVO(User user) {
        return UserRespVO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .openId(user.getOpenid())
                .createTime(user.getCreateTime() != null ? LocalDateTime.parse(user.getCreateTime().toString()) : null)
                .build();
    }
}
