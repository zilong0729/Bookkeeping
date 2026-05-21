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

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "管理员模块", description = "管理员相关接口")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    @OperLog("管理员登录")
    @Operation(summary = "管理员登录", description = "管理员使用账号密码登录")
    public Result<LoginVO> adminLogin(@Valid @RequestBody AdminLoginDTO loginDTO) {
        LoginVO loginVO = adminService.adminLogin(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    @GetMapping("/users")
    @RequireAdmin
    @OperLog("管理员查询用户列表")
    @Operation(summary = "查询用户列表", description = "管理员分页查询用户列表")
    public Result<PageResult<UserVO>> listUsers(AdminUserQueryDTO queryDTO) {
        PageResult<UserVO> result = adminService.listUsers(queryDTO);
        return Result.success(result);
    }

    @GetMapping("/users/{userId}")
    @RequireAdmin
    @OperLog("管理员查看用户详情")
    @Operation(summary = "查看用户详情", description = "管理员查看指定用户的详细信息")
    public Result<UserVO> getUserDetail(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        UserVO userVO = adminService.getUserDetail(userId);
        return Result.success(userVO);
    }

    @PutMapping("/users/{userId}")
    @RequireAdmin
    @OperLog("管理员操作用户")
    @Operation(summary = "操作用户", description = "管理员可以禁用/启用用户")
    public Result<UserVO> operateUser(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId,
            @Valid @RequestBody AdminOperateUserDTO operateDTO) {
        UserVO userVO = adminService.operateUser(userId, operateDTO);
        return Result.success("操作成功", userVO);
    }

    @GetMapping("/users/{userId}/records")
    @RequireAdmin
    @OperLog("管理员查看用户账单")
    @Operation(summary = "查看用户账单", description = "管理员查看指定用户的账单记录")
    public Result<PageResult<?>> listUserRecords(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId,
            @Parameter(description = "账单类型：1-支出，2-收入") @RequestParam(required = false) Integer type,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size) {
        PageResult<?> result = adminService.listUserRecords(userId, type, startDate, endDate, current, size);
        return Result.success(result);
    }

    @GetMapping("/users/{userId}/statistics")
    @RequireAdmin
    @OperLog("管理员查看用户统计")
    @Operation(summary = "查看用户统计", description = "管理员查看指定用户的收支统计")
    public Result<?> getUserStatistics(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        Object statistics = adminService.getUserStatistics(userId, startDate, endDate);
        return Result.success(statistics);
    }

    @GetMapping("/logs")
    @RequireAdmin
    @OperLog("管理员查询操作日志")
    @Operation(summary = "查询操作日志", description = "管理员分页查询系统操作日志")
    public Result<PageResult<OperationLogVO>> listLogs(LogQueryDTO queryDTO) {
        PageResult<OperationLogVO> result = adminService.listLogs(queryDTO);
        return Result.success(result);
    }
}
