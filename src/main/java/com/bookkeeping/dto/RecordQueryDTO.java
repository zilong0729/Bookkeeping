package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "账单查询DTO")
public class RecordQueryDTO {

    @Schema(description = "类型：1-支出，2-收入")
    private Integer type;

    @Schema(description = "类别ID")
    private Long categoryId;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "当前页")
    private Long current = 1L;

    @Schema(description = "每页条数")
    private Long size = 10L;
}
