package com.bookkeeping.controller;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.annotation.RateLimit;
import com.bookkeeping.service.RecordService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.req.CreateRecordReqVO;
import com.bookkeeping.vo.req.IdReqVO;
import com.bookkeeping.vo.req.RecordListReqVO;
import com.bookkeeping.vo.req.StatisticsReqVO;
import com.bookkeeping.vo.req.UnifiedRequest;
import com.bookkeeping.vo.req.UpdateRecordReqVO;
import com.bookkeeping.vo.resp.RecordRespVO;
import com.bookkeeping.vo.resp.Result;
import com.bookkeeping.vo.resp.StatisticsRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/record")
@RequiredArgsConstructor
@Tag(name = "账单模块", description = "账单相关接口")
public class RecordController {

    private final RecordService recordService;

    @PostMapping
    @OperLog("创建账单")
    @RateLimit(limitType = RateLimit.LimitType.USER, permitsPerSecond = 5, message = "创建账单过于频繁，请稍后重试")
    @Operation(summary = "创建账单", description = "创建一条新的账单记录")
    public Result<RecordRespVO> create(@Valid @RequestBody UnifiedRequest<CreateRecordReqVO> req) {
        Long userId = UserContext.getUserId();
        RecordRespVO respVO = recordService.createRecord(userId, req.getData());
        return Result.success("创建成功", respVO);
    }

    @PostMapping("/update")
    @OperLog("更新账单")
    @RateLimit(limitType = RateLimit.LimitType.USER, permitsPerSecond = 10, message = "更新账单过于频繁，请稍后重试")
    @Operation(summary = "更新账单", description = "更新指定的账单记录")
    public Result<RecordRespVO> update(@Valid @RequestBody UnifiedRequest<UpdateRecordReqVO> req) {
        Long userId = UserContext.getUserId();
        RecordRespVO respVO = recordService.updateRecord(userId, req.getData());
        return Result.success("更新成功", respVO);
    }

    @PostMapping("/delete")
    @OperLog("删除账单")
    @RateLimit(limitType = RateLimit.LimitType.USER, permitsPerSecond = 10, message = "删除账单过于频繁，请稍后重试")
    @Operation(summary = "删除账单", description = "删除指定的账单记录")
    public Result<Void> delete(@Valid @RequestBody UnifiedRequest<IdReqVO> req) {
        Long userId = UserContext.getUserId();
        recordService.deleteRecord(userId, req.getData().getId());
        return Result.success("删除成功");
    }

    @PostMapping("/detail")
    @Operation(summary = "获取账单详情", description = "获取指定账单的详细信息")
    public Result<RecordRespVO> detail(@Valid @RequestBody UnifiedRequest<IdReqVO> req) {
        Long userId = UserContext.getUserId();
        RecordRespVO respVO = recordService.getRecordDetail(userId, req.getData().getId());
        return Result.success(respVO);
    }

    @PostMapping("/list")
    @RateLimit(limitType = RateLimit.LimitType.USER, permitsPerSecond = 20, windowMs = 1000, message = "查询过于频繁，请稍后重试")
    @Operation(summary = "获取账单列表", description = "分页查询账单记录")
    public Result<List<RecordRespVO>> list(@Valid @RequestBody UnifiedRequest<RecordListReqVO> req) {
        Long userId = UserContext.getUserId();
        List<RecordRespVO> list = recordService.getRecordList(userId, req.getData());
        return Result.success(list);
    }

    @PostMapping("/statistics")
    @RateLimit(limitType = RateLimit.LimitType.USER, permitsPerSecond = 30, windowMs = 1000, message = "查询统计过于频繁，请稍后重试")
    @Operation(summary = "获取统计数据", description = "获取指定时间段的收支统计数据")
    public Result<StatisticsRespVO> statistics(@Valid @RequestBody UnifiedRequest<StatisticsReqVO> req) {
        Long userId = UserContext.getUserId();
        StatisticsRespVO statistics = recordService.getStatistics(userId, req.getData());
        return Result.success(statistics);
    }
}
