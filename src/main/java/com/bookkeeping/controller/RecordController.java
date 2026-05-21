package com.bookkeeping.controller;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.common.PageResult;
import com.bookkeeping.common.Result;
import com.bookkeeping.dto.RecordDTO;
import com.bookkeeping.dto.RecordQueryDTO;
import com.bookkeeping.service.RecordService;
import com.bookkeeping.utils.UserContext;
import com.bookkeeping.vo.RecordVO;
import com.bookkeeping.vo.StatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/record")
@RequiredArgsConstructor
@Tag(name = "账单模块", description = "账单相关接口")
public class RecordController {

    private final RecordService recordService;

    @PostMapping
    @OperLog("创建账单")
    @Operation(summary = "创建账单", description = "创建一条新的账单记录")
    public Result<RecordVO> createRecord(@Valid @RequestBody RecordDTO recordDTO) {
        Long userId = UserContext.getUserId();
        RecordVO vo = recordService.createRecord(userId, recordDTO);
        return Result.success("创建成功", vo);
    }

    @PutMapping("/{id}")
    @OperLog("更新账单")
    @Operation(summary = "更新账单", description = "更新指定的账单记录")
    public Result<RecordVO> updateRecord(
            @Parameter(description = "账单ID") @PathVariable("id") Long recordId,
            @Valid @RequestBody RecordDTO recordDTO) {
        Long userId = UserContext.getUserId();
        RecordVO vo = recordService.updateRecord(userId, recordId, recordDTO);
        return Result.success("更新成功", vo);
    }

    @DeleteMapping("/{id}")
    @OperLog("删除账单")
    @Operation(summary = "删除账单", description = "删除指定的账单记录")
    public Result<Void> deleteRecord(
            @Parameter(description = "账单ID") @PathVariable("id") Long recordId) {
        Long userId = UserContext.getUserId();
        recordService.deleteRecord(userId, recordId);
        return Result.success("删除成功", null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取账单详情", description = "获取指定账单的详细信息")
    public Result<RecordVO> getRecordDetail(
            @Parameter(description = "账单ID") @PathVariable("id") Long recordId) {
        Long userId = UserContext.getUserId();
        RecordVO vo = recordService.getRecordDetail(userId, recordId);
        return Result.success(vo);
    }

    @GetMapping("/list")
    @Operation(summary = "获取账单列表", description = "分页查询账单记录")
    public Result<PageResult<RecordVO>> getRecordList(RecordQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        PageResult<RecordVO> result = recordService.getRecordList(userId, queryDTO);
        return Result.success(result);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取统计数据", description = "获取指定时间段的收支统计数据")
    public Result<StatisticsVO> getStatistics(RecordQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        StatisticsVO statistics = recordService.getStatistics(userId, queryDTO);
        return Result.success(statistics);
    }
}
