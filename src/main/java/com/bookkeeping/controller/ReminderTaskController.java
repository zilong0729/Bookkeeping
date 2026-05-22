package com.bookkeeping.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.common.PageResult;
import com.bookkeeping.common.Result;
import com.bookkeeping.dto.ReminderTaskDTO;
import com.bookkeeping.service.ReminderTaskService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.ReminderTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @PostMapping
    @OperLog("创建提醒任务")
    @Operation(summary = "创建提醒任务", description = "创建一个新的提醒任务")
    public Result<ReminderTaskVO> createTask(@Valid @RequestBody ReminderTaskDTO dto) {
        Long userId = UserContext.getUserId();
        ReminderTaskVO vo = reminderTaskService.createTask(userId, dto);
        return Result.success("创建成功", vo);
    }

    @PutMapping("/{id}")
    @OperLog("更新提醒任务")
    @Operation(summary = "更新提醒任务", description = "更新指定的提醒任务")
    public Result<ReminderTaskVO> updateTask(
            @Parameter(description = "任务ID") @PathVariable("id") Long id,
            @Valid @RequestBody ReminderTaskDTO dto) {
        Long userId = UserContext.getUserId();
        ReminderTaskVO vo = reminderTaskService.updateTask(userId, id, dto);
        return Result.success("更新成功", vo);
    }

    @DeleteMapping("/{id}")
    @OperLog("删除提醒任务")
    @Operation(summary = "删除提醒任务", description = "删除指定的提醒任务")
    public Result<Void> deleteTask(
            @Parameter(description = "任务ID") @PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        reminderTaskService.deleteTask(userId, id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取提醒任务详情", description = "获取指定提醒任务的详细信息")
    public Result<ReminderTaskVO> getTaskDetail(
            @Parameter(description = "任务ID") @PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        ReminderTaskVO vo = reminderTaskService.getTaskDetail(userId, id);
        return Result.success(vo);
    }

    @GetMapping("/list")
    @Operation(summary = "获取提醒任务列表", description = "分页查询提醒任务列表")
    public Result<PageResult<ReminderTaskVO>> getTaskList(
            @Parameter(description = "状态：0-待发送，1-已发送，2-已取消") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size) {
        Long userId = UserContext.getUserId();
        Page<ReminderTaskVO> page = reminderTaskService.getTaskList(userId, status, current, size);
        return Result.success(PageResult.from(page));
    }

    @PostMapping("/{id}/cancel")
    @OperLog("取消提醒任务")
    @Operation(summary = "取消提醒任务", description = "取消指定的提醒任务")
    public Result<Void> cancelTask(
            @Parameter(description = "任务ID") @PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        reminderTaskService.cancelTask(userId, id);
        return Result.success("取消成功", null);
    }

    @GetMapping("/{id}/share-params")
    @Operation(summary = "生成分享参数", description = "生成提醒任务的分享参数")
    public Result<Map<String, String>> generateShareParams(
            @Parameter(description = "任务ID") @PathVariable("id") Long id,
            @Parameter(description = "联系人ID") @RequestParam(required = false) Long contactId) {
        Long userId = UserContext.getUserId();
        String params = reminderTaskService.generateShareParams(userId, id, contactId);
        Map<String, String> result = new HashMap<>();
        result.put("shareParams", params);
        return Result.success(result);
    }
}
