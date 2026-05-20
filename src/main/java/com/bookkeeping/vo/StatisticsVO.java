package com.bookkeeping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 统计VO
 */
@Data
@Schema(description = "账单统计信息")
public class StatisticsVO {

    @Schema(description = "总收入")
    private BigDecimal totalIncome;

    @Schema(description = "总支出")
    private BigDecimal totalExpense;

    @Schema(description = "结余（收入-支出）")
    private BigDecimal balance;
}
