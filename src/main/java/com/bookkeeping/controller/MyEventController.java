package com.bookkeeping.controller;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.service.MyEventService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.req.CreateEventReqVO;
import com.bookkeeping.vo.req.EventListReqVO;
import com.bookkeeping.vo.req.IdReqVO;
import com.bookkeeping.vo.req.UnifiedRequest;
import com.bookkeeping.vo.req.UpdateEventReqVO;
import com.bookkeeping.vo.resp.EventRespVO;
import com.bookkeeping.vo.resp.PageResult;
import com.bookkeeping.vo.resp.Result;
import io.swagger.v3.oas.annotations.Operation;
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

    @PostMapping("/create")
    @OperLog("创建我的事件")
    @Operation(summary = "创建我的事件", description = "创建我的事件（结婚、生日等）")
    public Result<EventRespVO> create(@Valid @RequestBody UnifiedRequest<CreateEventReqVO> req) {
        Long userId = UserContext.getUserId();
        EventRespVO respVO = myEventService.createEvent(userId, req.getData());
        return Result.success("创建成功", respVO);
    }

    @PostMapping("/update")
    @OperLog("更新我的事件")
    @Operation(summary = "更新我的事件", description = "更新我的事件")
    public Result<EventRespVO> update(@Valid @RequestBody UnifiedRequest<UpdateEventReqVO> req) {
        Long userId = UserContext.getUserId();
        EventRespVO respVO = myEventService.updateEvent(userId, req.getData());
        return Result.success("更新成功", respVO);
    }

    @PostMapping("/delete")
    @OperLog("删除我的事件")
    @Operation(summary = "删除我的事件", description = "删除我的事件")
    public Result<Void> delete(@Valid @RequestBody UnifiedRequest<IdReqVO> req) {
        Long userId = UserContext.getUserId();
        myEventService.deleteEvent(userId, req.getData().getId());
        return Result.success("删除成功");
    }

    @PostMapping("/detail")
    @Operation(summary = "获取我的事件详情", description = "获取我的事件详情")
    public Result<EventRespVO> detail(@Valid @RequestBody UnifiedRequest<IdReqVO> req) {
        Long userId = UserContext.getUserId();
        EventRespVO respVO = myEventService.getEventDetail(userId, req.getData().getId());
        return Result.success(respVO);
    }

    @PostMapping("/list")
    @Operation(summary = "获取我的事件列表", description = "分页查询我的事件列表")
    public Result<PageResult<EventRespVO>> list(@Valid @RequestBody UnifiedRequest<EventListReqVO> req) {
        Long userId = UserContext.getUserId();
        PageResult<EventRespVO> result = myEventService.getEventList(userId, req.getData());
        return Result.success(result);
    }
}
