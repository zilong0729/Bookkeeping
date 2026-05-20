package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账单DTO
 */
@Data
@Schema(description = "账单请求参数")
public class RecordDTO {

    @NotNull(message = "类别ID不能为空")
    @Schema(description = "类别ID", required = true)
    private Long categoryId;

    @NotNull(message = "类型不能为空")
    @Schema(description = "类型：1-收入，2-支出", required = true)
    private Integer type;

    @NotNull(message = "金额不能为空")
    @Schema(description = "金额", required = true)
    private BigDecimal amount;

    @Schema(description = "往来对象姓名")
    private String contactName;

    @NotNull(message = "日期不能为空")
    @Schema(description = "账单日期", required = true)
    private LocalDate recordDate;

    @Schema(description = "备注")
    private String remark;
}
