package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 账单查询DTO
 */
@Data
@Schema(description = "账单查询参数")
public class RecordQueryDTO {

    @Schema(description = "类型：1-收入，2-支出")
    private Integer type;

    @Schema(description = "类别ID")
    private Long categoryId;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "往来对象姓名（模糊查询）")
    private String contactName;

    @Schema(description = "页码，默认1")
    private Long current = 1L;

    @Schema(description = "每页大小，默认10")
    private Long size = 10L;
}
