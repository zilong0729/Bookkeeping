package com.bookkeeping.service;

import com.bookkeeping.dto.UpdateUserDTO;
import com.bookkeeping.dto.WxLoginDTO;
import com.bookkeeping.vo.LoginVO;
import com.bookkeeping.vo.UserVO;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 微信小程序登录
     */
    LoginVO wxLogin(WxLoginDTO loginDTO) throws JsonProcessingException;

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 更新用户信息
     */
    UserVO updateUser(Long userId, UpdateUserDTO updateDTO);

    /**
     * 用户登出
     */
    void logout(Long userId);
}
