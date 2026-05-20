package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 操作日志查询DTO
 */
@Data
@Schema(description = "操作日志查询参数")
public class LogQueryDTO {

    @Schema(description = "操作用户ID")
    private Long userId;

    @Schema(description = "操作描述（模糊查询）")
    private String operation;

    @Schema(description = "操作状态：0-失败，1-成功")
    private Integer status;

    @Schema(description = "开始时间（yyyy-MM-dd HH:mm:ss）")
    private String startTime;

    @Schema(description = "结束时间（yyyy-MM-dd HH:mm:ss）")
    private String endTime;

    @Schema(description = "页码，默认1")
    private Long current = 1L;

    @Schema(description = "每页大小，默认10")
    private Long size = 10L;
}
