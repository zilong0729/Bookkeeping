package com.bookkeeping.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookkeeping.entity.MyEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MyEventMapper extends BaseMapper<MyEvent> {
}
