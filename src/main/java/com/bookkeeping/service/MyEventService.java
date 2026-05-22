package com.bookkeeping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.dto.MyEventDTO;
import com.bookkeeping.vo.MyEventVO;

import java.util.List;

public interface MyEventService {
    MyEventVO createEvent(Long userId, MyEventDTO dto);
    MyEventVO updateEvent(Long userId, Long id, MyEventDTO dto);
    void deleteEvent(Long userId, Long id);
    MyEventVO getEventDetail(Long userId, Long id);
    Page<MyEventVO> getEventList(Long userId, Integer status, Long current, Long size);
    List<MyEventVO> getEventsToPush();
    void markAsPushed(Long id);
}
