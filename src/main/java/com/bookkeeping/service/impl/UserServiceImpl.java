package com.bookkeeping.service.impl;

import com.alibaba.fastjson2.JSON;
import com.bookkeeping.dto.UpdateUserDTO;
import com.bookkeeping.dto.WxLoginDTO;
import com.bookkeeping.entity.User;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.UserMapper;
import com.bookkeeping.service.UserService;
import com.bookkeeping.utils.JwtUtil;
import com.bookkeeping.utils.RedisUtil;
import com.bookkeeping.vo.LoginVO;
import com.bookkeeping.vo.UserVO;
import com.bookkeeping.vo.WxSessionVO;
import com.bookkeeping.config.WxMiniAppConfig;
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

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO wxLogin(WxLoginDTO loginDTO) {
        // 1. 调用微信接口获取openid和session_key
        String url = wxMiniAppConfig.getAuthUrl(loginDTO.getCode());
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new BusinessException("微信登录失败，请稍后重试");
        }

        WxSessionVO sessionVO = JSON.parseObject(response.getBody(), WxSessionVO.class);

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
            // 创建新用户
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(sessionVO.getUnionid());
            user.setNickname(loginDTO.getNickname());
            user.setAvatarUrl(loginDTO.getAvatarUrl());
            user.setStatus(1);
            userMapper.insert(user);
            log.info("创建新用户: userId={}, openid={}", user.getId(), openid);
        } else {
            // 更新用户信息（如果提供了）
            if (loginDTO.getNickname() != null && !loginDTO.getNickname().isEmpty()) {
                user.setNickname(loginDTO.getNickname());
            }
            if (loginDTO.getAvatarUrl() != null && !loginDTO.getAvatarUrl().isEmpty()) {
                user.setAvatarUrl(loginDTO.getAvatarUrl());
            }
            userMapper.updateById(user);
        }

        // 3. 生成token
        String token = jwtUtil.generateToken(user.getId(), openid, user.getRole());

        // 4. 缓存token到Redis
        redisUtil.cacheUserToken(user.getId(), token, 7, TimeUnit.DAYS);

        // 5. 组装返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setExpiresIn(7 * 24 * 60 * 60L); // 7天
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

        // 更新用户信息
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
        redisUtil.deleteUserToken(userId);
        log.info("用户登出: userId={}", userId);
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
