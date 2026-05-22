package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账单列表查询请求VO")
public class RecordListReqVO {

    @Schema(description = "类型：1-支出，2-收入")
    private Integer type;

    @Schema(description = "类别ID")
    private Long categoryId;

    @Schema(description = "联系人ID")
    private Long contactId;

    @Schema(description = "联系人名称")
    private String contactName;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "当前页码", example = "1")
    private Long current = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long size = 10L;
}
