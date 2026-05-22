package com.bookkeeping.controller;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.service.ReminderTaskService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.req.*;
import com.bookkeeping.vo.resp.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/reminder")
@RequiredArgsConstructor
@Tag(name = "提醒任务管理", description = "提醒任务相关接口")
public class ReminderTaskController {

    private final ReminderTaskService reminderTaskService;

    @PostMapping("/create")
    @OperLog("创建提醒任务")
    @Operation(summary = "创建提醒任务", description = "创建一个新的提醒任务")
    public Result<ReminderTaskRespVO> createTask(@Valid @RequestBody UnifiedRequest<CreateReminderTaskReqVO> req) {
        Long userId = UserContext.getUserId();
        ReminderTaskRespVO vo = reminderTaskService.createTask(userId, req.getData());
        return Result.success("创建成功", vo);
    }

    @PostMapping("/update")
    @OperLog("更新提醒任务")
    @Operation(summary = "更新提醒任务", description = "更新指定的提醒任务")
    public Result<ReminderTaskRespVO> updateTask(@Valid @RequestBody UnifiedRequest<UpdateReminderTaskReqVO> req) {
        Long userId = UserContext.getUserId();
        ReminderTaskRespVO vo = reminderTaskService.updateTask(userId, req.getData());
        return Result.success("更新成功", vo);
    }

    @PostMapping("/delete")
    @OperLog("删除提醒任务")
    @Operation(summary = "删除提醒任务", description = "删除指定的提醒任务")
    public Result<Void> deleteTask(@Valid @RequestBody UnifiedRequest<IdReqVO> req) {
        Long userId = UserContext.getUserId();
        reminderTaskService.deleteTask(userId, req.getData().getId());
        return Result.success("删除成功");
    }

    @PostMapping("/detail")
    @Operation(summary = "获取提醒任务详情", description = "获取指定提醒任务的详细信息")
    public Result<ReminderTaskRespVO> getTaskDetail(@Valid @RequestBody UnifiedRequest<IdReqVO> req) {
        Long userId = UserContext.getUserId();
        ReminderTaskRespVO vo = reminderTaskService.getTaskDetail(userId, req.getData().getId());
        return Result.success(vo);
    }

    @PostMapping("/list")
    @Operation(summary = "获取提醒任务列表", description = "分页查询提醒任务列表")
    public Result<PageResult<ReminderTaskRespVO>> getTaskList(@Valid @RequestBody UnifiedRequest<ReminderTaskListReqVO> req) {
        Long userId = UserContext.getUserId();
        PageResult<ReminderTaskRespVO> page = reminderTaskService.getTaskList(userId, req.getData());
        return Result.success(page);
    }

    @PostMapping("/cancel")
    @OperLog("取消提醒任务")
    @Operation(summary = "取消提醒任务", description = "取消指定的提醒任务")
    public Result<Void> cancelTask(@Valid @RequestBody UnifiedRequest<IdReqVO> req) {
        Long userId = UserContext.getUserId();
        reminderTaskService.cancelTask(userId, req.getData().getId());
        return Result.success("取消成功");
    }

    @PostMapping("/share-params")
    @Operation(summary = "生成分享参数", description = "生成提醒任务的分享参数")
    public Result<Map<String, String>> generateShareParams(@Valid @RequestBody UnifiedRequest<GenerateShareParamsReqVO> req) {
        Long userId = UserContext.getUserId();
        String params = reminderTaskService.generateShareParams(userId, req.getData().getTaskId(), req.getData().getContactId());
        Map<String, String> result = new HashMap<>();
        result.put("shareParams", params);
        return Result.success(result);
    }
}
