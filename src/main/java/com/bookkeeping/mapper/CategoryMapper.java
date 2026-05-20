package com.bookkeeping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookkeeping.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 类别Mapper接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 查询用户的类别列表（包括系统默认类别）
     */
    @Select("SELECT * FROM category WHERE (user_id = 0 OR user_id = #{userId}) AND deleted = 0 ORDER BY sort_order")
    List<Category> selectByUserId(@Param("userId") Long userId);
}
