package com.bookkeeping.controller;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.annotation.RequireAdmin;
import com.bookkeeping.common.PageResult;
import com.bookkeeping.common.Result;
import com.bookkeeping.dto.*;
import com.bookkeeping.service.AdminService;
import com.bookkeeping.vo.LoginVO;
import com.bookkeeping.vo.OperationLogVO;
import com.bookkeeping.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "管理员接口", description = "管理员登录、用户管理、日志查询等接口")
public class AdminController {

    private final AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "使用管理员账号密码登录")
    @OperLog("管理员登录")
    public Result<LoginVO> adminLogin(@Valid @RequestBody AdminLoginDTO loginDTO) {
        LoginVO loginVO = adminService.adminLogin(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 查询所有用户列表
     */
    @GetMapping("/users")
    @Operation(summary = "查询所有用户", description = "分页查询所有用户，支持按昵称、手机号、状态、角色筛选")
    @RequireAdmin
    @OperLog("管理员查询用户列表")
    public Result<PageResult<UserVO>> listUsers(AdminUserQueryDTO queryDTO) {
        PageResult<UserVO> result = adminService.listUsers(queryDTO);
        return Result.success(result);
    }

    /**
     * 查看指定用户详情
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "查看用户详情", description = "查看指定用户的详细信息")
    @RequireAdmin
    @OperLog("管理员查看用户详情")
    public Result<UserVO> getUserDetail(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId) {
        UserVO userVO = adminService.getUserDetail(userId);
        return Result.success(userVO);
    }

    /**
     * 操作用户（禁用/启用、设置角色）
     */
    @PutMapping("/users/{userId}")
    @Operation(summary = "操作用户", description = "禁用/启用用户、设置用户角色")
    @RequireAdmin
    @OperLog("管理员操作用户")
    public Result<UserVO> operateUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId,
            @Valid @RequestBody AdminOperateUserDTO operateDTO) {
        UserVO userVO = adminService.operateUser(userId, operateDTO);
        return Result.success("操作成功", userVO);
    }

    /**
     * 查看指定用户的账单列表
     */
    @GetMapping("/users/{userId}/records")
    @Operation(summary = "查看用户账单", description = "查看指定用户的账单列表")
    @RequireAdmin
    @OperLog("管理员查看用户账单")
    public Result<PageResult<?>> listUserRecords(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId,
            @Parameter(description = "类型：1-收入，2-支出")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "开始日期（yyyy-MM-dd）")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）")
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        PageResult<?> result = adminService.listUserRecords(userId, type, startDate, endDate, current, size);
        return Result.success(result);
    }

    /**
     * 查看指定用户的统计信息
     */
    @GetMapping("/users/{userId}/statistics")
    @Operation(summary = "查看用户统计", description = "查看指定用户的收支统计信息")
    @RequireAdmin
    @OperLog("管理员查看用户统计")
    public Result<?> getUserStatistics(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") Long userId,
            @Parameter(description = "开始日期（yyyy-MM-dd）")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（yyyy-MM-dd）")
            @RequestParam(required = false) String endDate) {
        Object statistics = adminService.getUserStatistics(userId, startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 查询操作日志
     */
    @GetMapping("/logs")
    @Operation(summary = "查询操作日志", description = "分页查询操作日志，支持按用户、操作描述、状态、时间筛选")
    @RequireAdmin
    @OperLog("管理员查询操作日志")
    public Result<PageResult<OperationLogVO>> listLogs(LogQueryDTO queryDTO) {
        PageResult<OperationLogVO> result = adminService.listLogs(queryDTO);
        return Result.success(result);
    }
}
