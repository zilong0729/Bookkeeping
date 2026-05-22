package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询操作日志请求VO")
public class LogQueryReqVO {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "当前页码")
    private Long current = 1L;

    @Schema(description = "每页条数")
    private Long size = 10L;
}
