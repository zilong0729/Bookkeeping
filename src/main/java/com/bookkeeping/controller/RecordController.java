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

/**
 * 账单控制器
 */
@RestController
@RequestMapping("/record")
@RequiredArgsConstructor
@Tag(name = "账单管理", description = "账单CRUD及统计相关接口")
public class RecordController {

    private final RecordService recordService;

    /**
     * 创建账单
     */
    @PostMapping
    @Operation(summary = "创建账单", description = "新增一条账单记录")
    @OperLog("创建账单")
    public Result<RecordVO> createRecord(@Valid @RequestBody RecordDTO recordDTO) {
        Long userId = UserContext.getUserId();
        RecordVO vo = recordService.createRecord(userId, recordDTO);
        return Result.success("创建成功", vo);
    }

    /**
     * 更新账单
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新账单", description = "更新指定账单记录")
    @OperLog("更新账单")
    public Result<RecordVO> updateRecord(
            @Parameter(description = "账单ID", required = true)
            @PathVariable("id") Long recordId,
            @Valid @RequestBody RecordDTO recordDTO) {
        Long userId = UserContext.getUserId();
        RecordVO vo = recordService.updateRecord(userId, recordId, recordDTO);
        return Result.success("更新成功", vo);
    }

    /**
     * 删除账单
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除账单", description = "删除指定账单记录")
    @OperLog("删除账单")
    public Result<Void> deleteRecord(
            @Parameter(description = "账单ID", required = true)
            @PathVariable("id") Long recordId) {
        Long userId = UserContext.getUserId();
        recordService.deleteRecord(userId, recordId);
        return Result.success("删除成功", null);
    }

    /**
     * 获取账单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取账单详情", description = "获取指定账单的详细信息")
    public Result<RecordVO> getRecordDetail(
            @Parameter(description = "账单ID", required = true)
            @PathVariable("id") Long recordId) {
        Long userId = UserContext.getUserId();
        RecordVO vo = recordService.getRecordDetail(userId, recordId);
        return Result.success(vo);
    }

    /**
     * 查询账单列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询账单列表", description = "分页查询账单列表，支持多种筛选条件")
    public Result<PageResult<RecordVO>> getRecordList(RecordQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        PageResult<RecordVO> result = recordService.getRecordList(userId, queryDTO);
        return Result.success(result);
    }

    /**
     * 统计账单金额
     */
    @GetMapping("/statistics")
    @Operation(summary = "统计账单金额", description = "按日期范围统计收入、支出总额")
    public Result<StatisticsVO> getStatistics(RecordQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        StatisticsVO statistics = recordService.getStatistics(userId, queryDTO);
        return Result.success(statistics);
    }
}
