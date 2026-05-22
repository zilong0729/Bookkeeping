package com.bookkeeping.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.common.PageResult;
import com.bookkeeping.common.Result;
import com.bookkeeping.dto.MyEventDTO;
import com.bookkeeping.service.MyEventService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.MyEventVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/my-event")
@RequiredArgsConstructor
@Tag(name = "我的事件管理", description = "我的事件相关接口")
public class MyEventController {

    private final MyEventService myEventService;

    @PostMapping
    @OperLog("创建我的事件")
    @Operation(summary = "创建我的事件", description = "创建我的事件（结婚、生日等）")
    public Result<MyEventVO> createEvent(@Valid @RequestBody MyEventDTO dto) {
        Long userId = UserContext.getUserId();
        MyEventVO vo = myEventService.createEvent(userId, dto);
        return Result.success("创建成功", vo);
    }

    @PutMapping("/{id}")
    @OperLog("更新我的事件")
    @Operation(summary = "更新我的事件", description = "更新我的事件")
    public Result<MyEventVO> updateEvent(
            @Parameter(description = "事件ID") @PathVariable("id") Long id,
            @Valid @RequestBody MyEventDTO dto) {
        Long userId = UserContext.getUserId();
        MyEventVO vo = myEventService.updateEvent(userId, id, dto);
        return Result.success("更新成功", vo);
    }

    @DeleteMapping("/{id}")
    @OperLog("删除我的事件")
    @Operation(summary = "删除我的事件", description = "删除我的事件")
    public Result<Void> deleteEvent(
            @Parameter(description = "事件ID") @PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        myEventService.deleteEvent(userId, id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取我的事件详情", description = "获取我的事件详情")
    public Result<MyEventVO> getEventDetail(
            @Parameter(description = "事件ID") @PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        MyEventVO vo = myEventService.getEventDetail(userId, id);
        return Result.success(vo);
    }

    @GetMapping("/list")
    @Operation(summary = "获取我的事件列表", description = "分页查询我的事件列表")
    public Result<PageResult<MyEventVO>> getEventList(
            @Parameter(description = "状态：0-未推送，1-已推送") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size) {
        Long userId = UserContext.getUserId();
        Page<MyEventVO> page = myEventService.getEventList(userId, status, current, size);
        return Result.success(PageResult.from(page));
    }
}
