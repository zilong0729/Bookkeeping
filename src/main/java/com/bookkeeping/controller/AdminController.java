package com.bookkeeping.controller;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.annotation.RequireAdmin;
import com.bookkeeping.service.AdminService;
import com.bookkeeping.vo.req.*;
import com.bookkeeping.vo.resp.*;
import io.swagger.v3.oas.annotations.Operation;
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
    public Result<LoginRespVO> adminLogin(@Valid @RequestBody UnifiedRequest<AdminLoginReqVO> req) {
        LoginRespVO respVO = adminService.adminLogin(req.getData());
        return Result.success("登录成功", respVO);
    }

    @PostMapping("/users/list")
    @RequireAdmin
    @OperLog("管理员查询用户列表")
    @Operation(summary = "查询用户列表", description = "管理员分页查询用户列表")
    public Result<PageResult<UserRespVO>> listUsers(@Valid @RequestBody UnifiedRequest<AdminUserQueryReqVO> req) {
        PageResult<UserRespVO> result = adminService.listUsers(req.getData());
        return Result.success(result);
    }

    @PostMapping("/users/detail")
    @RequireAdmin
    @OperLog("管理员查看用户详情")
    @Operation(summary = "查看用户详情", description = "管理员查看指定用户的详细信息")
    public Result<UserRespVO> getUserDetail(@Valid @RequestBody UnifiedRequest<IdReqVO> req) {
        UserRespVO userVO = adminService.getUserDetail(req.getData().getId());
        return Result.success(userVO);
    }

    @PostMapping("/users/operate")
    @RequireAdmin
    @OperLog("管理员操作用户")
    @Operation(summary = "操作用户", description = "管理员可以禁用/启用用户")
    public Result<UserRespVO> operateUser(@Valid @RequestBody UnifiedRequest<AdminOperateUserReqVO> req) {
        UserRespVO userVO = adminService.operateUser(req.getData());
        return Result.success("操作成功", userVO);
    }

    @PostMapping("/users/records")
    @RequireAdmin
    @OperLog("管理员查看用户账单")
    @Operation(summary = "查看用户账单", description = "管理员查看指定用户的账单记录")
    public Result<PageResult<RecordRespVO>> listUserRecords(@Valid @RequestBody UnifiedRequest<AdminUserRecordsReqVO> req) {
        PageResult<RecordRespVO> result = adminService.listUserRecords(req.getData());
        return Result.success(result);
    }

    @PostMapping("/users/statistics")
    @RequireAdmin
    @OperLog("管理员查看用户统计")
    @Operation(summary = "查看用户统计", description = "管理员查看指定用户的收支统计")
    public Result<StatisticsRespVO> getUserStatistics(@Valid @RequestBody UnifiedRequest<AdminUserStatisticsReqVO> req) {
        StatisticsRespVO statistics = adminService.getUserStatistics(req.getData());
        return Result.success(statistics);
    }

    @PostMapping("/logs/list")
    @RequireAdmin
    @OperLog("管理员查询操作日志")
    @Operation(summary = "查询操作日志", description = "管理员分页查询系统操作日志")
    public Result<PageResult<OperationLogRespVO>> listLogs(@Valid @RequestBody UnifiedRequest<LogQueryReqVO> req) {
        PageResult<OperationLogRespVO> result = adminService.listLogs(req.getData());
        return Result.success(result);
    }
}
