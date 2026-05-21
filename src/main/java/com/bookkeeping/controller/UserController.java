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
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户模块", description = "用户相关接口")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    @OperLog("用户微信登录")
    @RateLimit(permitsPerSecond = 5, message = "登录请求过于频繁，请稍后重试")
    @Operation(summary = "微信登录", description = "用户使用微信授权码登录")
    public Result<LoginVO> login(@Valid @RequestBody WxLoginDTO loginDTO) throws JsonProcessingException {
        LoginVO loginVO = userService.wxLogin(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserVO> getCurrentUser() {
        Long userId = UserContext.getUserId();
        UserVO userVO = userService.getCurrentUser(userId);
        return Result.success(userVO);
    }

    @PutMapping("/info")
    @OperLog("用户更新个人信息")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息")
    public Result<UserVO> updateUser(@Valid @RequestBody UpdateUserDTO updateDTO) {
        Long userId = UserContext.getUserId();
        UserVO userVO = userService.updateUser(userId, updateDTO);
        return Result.success("更新成功", userVO);
    }

    @PostMapping("/logout")
    @OperLog("用户登出")
    @Operation(summary = "用户登出", description = "用户退出登录，清除缓存")
    public Result<Void> logout() {
        Long userId = UserContext.getUserId();
        userService.logout(userId);
        return Result.success("登出成功", null);
    }
}
