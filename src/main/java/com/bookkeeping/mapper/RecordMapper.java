package com.bookkeeping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookkeeping.entity.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账单Mapper接口
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {

    /**
     * 统计指定日期范围内的金额总和
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM record WHERE user_id = #{userId} AND type = #{type} " +
            "AND record_date >= #{startDate} AND record_date <= #{endDate} AND deleted = 0")
    BigDecimal sumAmountByDateRange(@Param("userId") Long userId,
                                    @Param("type") Integer type,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);
}
