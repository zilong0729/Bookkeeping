package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建账单请求VO")
public class CreateRecordReqVO {

    @Schema(description = "类别ID", required = true)
    @NotNull(message = "类别ID不能为空")
    private Long categoryId;

    @Schema(description = "类型：1-支出，2-收入", required = true)
    @NotNull(message = "类型不能为空")
    private Integer type;

    @Schema(description = "金额", required = true)
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @Schema(description = "联系人ID")
    private Long contactId;

    @Schema(description = "联系人名称")
    private String contactName;

    @Schema(description = "记账日期")
    private LocalDate recordDate;

    @Schema(description = "备注")
    private String remark;
}
