package com.bookkeeping.service;

import com.bookkeeping.entity.MyEvent;
import com.bookkeeping.vo.req.CreateEventReqVO;
import com.bookkeeping.vo.req.EventListReqVO;
import com.bookkeeping.vo.req.UpdateEventReqVO;
import com.bookkeeping.vo.resp.EventRespVO;
import com.bookkeeping.vo.resp.PageResult;

import java.util.List;

public interface MyEventService {

    EventRespVO createEvent(Long userId, CreateEventReqVO reqVO);

    EventRespVO updateEvent(Long userId, UpdateEventReqVO reqVO);

    void deleteEvent(Long userId, Long id);

    EventRespVO getEventDetail(Long userId, Long id);

    PageResult<EventRespVO> getEventList(Long userId, EventListReqVO reqVO);

    List<MyEvent> getEventsToPush();

    void markAsPushed(Long id);
}
