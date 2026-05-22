package com.bookkeeping.controller;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.annotation.RateLimit;
import com.bookkeeping.service.UserService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.req.LoginReqVO;
import com.bookkeeping.vo.req.UnifiedRequest;
import com.bookkeeping.vo.req.UserUpdateReqVO;
import com.bookkeeping.vo.resp.LoginRespVO;
import com.bookkeeping.vo.resp.Result;
import com.bookkeeping.vo.resp.UserRespVO;
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
    public Result<LoginRespVO> login(@Valid @RequestBody UnifiedRequest<LoginReqVO> req) throws JsonProcessingException {
        LoginRespVO respVO = userService.wxLogin(req.getData());
        return Result.success("登录成功", respVO);
    }

    @PostMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserRespVO> getCurrentUser() {
        Long userId = UserContext.getUserId();
        UserRespVO respVO = userService.getCurrentUser(userId);
        return Result.success(respVO);
    }

    @PostMapping("/update")
    @OperLog("用户更新个人信息")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息")
    public Result<UserRespVO> updateUser(@Valid @RequestBody UnifiedRequest<UserUpdateReqVO> req) {
        Long userId = UserContext.getUserId();
        UserRespVO respVO = userService.updateUser(userId, req.getData());
        return Result.success("更新成功", respVO);
    }

    @PostMapping("/logout")
    @OperLog("用户登出")
    @Operation(summary = "用户登出", description = "用户退出登录，清除缓存")
    public Result<Void> logout() {
        Long userId = UserContext.getUserId();
        userService.logout(userId);
        return Result.success("登出成功");
    }
}
