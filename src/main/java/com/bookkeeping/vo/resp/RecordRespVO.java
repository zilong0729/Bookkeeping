package com.bookkeeping.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账单响应VO")
public class RecordRespVO {

    @Schema(description = "账单ID")
    private Long id;

    @Schema(description = "类别ID")
    private Long categoryId;

    @Schema(description = "类别名称")
    private String categoryName;

    @Schema(description = "类别图标")
    private String categoryIcon;

    @Schema(description = "类型：1-支出，2-收入")
    private Integer type;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "联系人ID")
    private Long contactId;

    @Schema(description = "联系人名称")
    private String contactName;

    @Schema(description = "记账日期")
    private LocalDate recordDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
