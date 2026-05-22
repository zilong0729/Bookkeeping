package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "账单DTO")
public class RecordDTO {

    @Schema(description = "类别ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "类别ID不能为空")
    private Long categoryId;

    @Schema(description = "类型：1-支出，2-收入", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "类型不能为空")
    private Integer type;

    @Schema(description = "金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系人ID")
    private Long contactId;

    @Schema(description = "账单日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "日期不能为空")
    private LocalDate recordDate;

    @Schema(description = "备注")
    private String remark;
}
