package com.bookkeeping.controller;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.annotation.RateLimit;
import com.bookkeeping.common.Result;
import com.bookkeeping.dto.UpdateUserDTO;
import com.bookkeeping.dto.WxLoginDTO;
import com.bookkeeping.service.UserService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.LoginVO;
import com.bookkeeping.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户登录、信息管理等接口")
public class UserController {

    private final UserService userService;

    /**
     * 微信小程序登录
     */
    @PostMapping("/login")
    @Operation(summary = "微信小程序登录", description = "使用微信code登录，获取token")
    @OperLog("用户微信登录")
    @RateLimit(permitsPerSecond = 5, message = "登录请求过于频繁，请稍后重试")
    public Result<LoginVO> login(@Valid @RequestBody WxLoginDTO loginDTO) {
        LoginVO loginVO = userService.wxLogin(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserVO> getCurrentUser() {
        Long userId = UserContext.getUserId();
        UserVO userVO = userService.getCurrentUser(userId);
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    @OperLog("用户更新个人信息")
    public Result<UserVO> updateUser(@Valid @RequestBody UpdateUserDTO updateDTO) {
        Long userId = UserContext.getUserId();
        UserVO userVO = userService.updateUser(userId, updateDTO);
        return Result.success("更新成功", userVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录，清除token")
    @OperLog("用户登出")
    public Result<Void> logout() {
        Long userId = UserContext.getUserId();
        userService.logout(userId);
        return Result.success("登出成功", null);
    }
}
