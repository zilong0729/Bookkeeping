package com.bookkeeping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "账单信息VO")
public class RecordVO {

    @Schema(description = "账单ID")
    private Long id;

    @Schema(description = "类别ID")
    private Long categoryId;

    @Schema(description = "类别名称")
    private String categoryName;

    @Schema(description = "类型：1-支出，2-收入")
    private Integer type;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系人ID")
    private Long contactId;

    @Schema(description = "账单日期")
    private LocalDate recordDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
