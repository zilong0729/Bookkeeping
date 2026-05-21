package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "操作日志查询DTO")
public class LogQueryDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "操作描述")
    private String operation;

    @Schema(description = "状态：0-失败，1-成功")
    private Integer status;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "当前页")
    private Long current = 1L;

    @Schema(description = "每页条数")
    private Long size = 10L;
}
