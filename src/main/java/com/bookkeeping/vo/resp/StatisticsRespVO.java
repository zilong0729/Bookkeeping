package com.bookkeeping.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统计响应VO")
public class StatisticsRespVO {

    @Schema(description = "总收入")
    private BigDecimal totalIncome;

    @Schema(description = "总支出")
    private BigDecimal totalExpense;

    @Schema(description = "结余")
    private BigDecimal balance;

    @Schema(description = "收入笔数")
    private Integer incomeCount;

    @Schema(description = "支出笔数")
    private Integer expenseCount;
}
