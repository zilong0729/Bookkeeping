package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.entity.Contact;
import com.bookkeeping.entity.MyEvent;
import com.bookkeeping.entity.Record;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.ContactMapper;
import com.bookkeeping.mapper.MyEventMapper;
import com.bookkeeping.mapper.RecordMapper;
import com.bookkeeping.service.MyEventService;
import com.bookkeeping.vo.req.CreateEventReqVO;
import com.bookkeeping.vo.req.EventListReqVO;
import com.bookkeeping.vo.req.UpdateEventReqVO;
import com.bookkeeping.vo.resp.ContactRespVO;
import com.bookkeeping.vo.resp.EventRespVO;
import com.bookkeeping.vo.resp.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyEventServiceImpl implements MyEventService {

    private final MyEventMapper myEventMapper;
    private final RecordMapper recordMapper;
    private final ContactMapper contactMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventRespVO createEvent(Long userId, CreateEventReqVO reqVO) {
        MyEvent event = new MyEvent();
        BeanUtils.copyProperties(reqVO, event);
        event.setUserId(userId);
        event.setPushStatus(0);
        if (reqVO.getAdvanceDays() == null) {
            event.setAdvanceDays(3);
        }
        myEventMapper.insert(event);
        return convertToRespVO(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventRespVO updateEvent(Long userId, UpdateEventReqVO reqVO) {
        MyEvent event = myEventMapper.selectById(reqVO.getId());
        if (event == null || event.getDeleted() == 1) {
            throw new BusinessException("事件不存在");
        }
        if (!event.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该事件");
        }

        BeanUtils.copyProperties(reqVO, event, "id", "userId", "createTime", "pushStatus", "pushTime");
        if (reqVO.getAdvanceDays() == null) {
            event.setAdvanceDays(3);
        }
        myEventMapper.updateById(event);
        return convertToRespVO(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEvent(Long userId, Long id) {
        MyEvent event = myEventMapper.selectById(id);
        if (event == null || event.getDeleted() == 1) {
            throw new BusinessException("事件不存在");
        }
        if (!event.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该事件");
        }
        myEventMapper.deleteById(id);
    }

    @Override
    public EventRespVO getEventDetail(Long userId, Long id) {
        MyEvent event = myEventMapper.selectById(id);
        if (event == null || event.getDeleted() == 1 || !event.getUserId().equals(userId)) {
            throw new BusinessException("事件不存在");
        }
        return convertToRespVO(event);
    }

    @Override
    public PageResult<EventRespVO> getEventList(Long userId, EventListReqVO reqVO) {
        LambdaQueryWrapper<MyEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MyEvent::getUserId, userId);
        wrapper.eq(MyEvent::getDeleted, 0);
        if (reqVO != null && reqVO.getStatus() != null) {
            wrapper.eq(MyEvent::getPushStatus, reqVO.getStatus());
        }
        wrapper.orderByDesc(MyEvent::getEventDate);
        wrapper.orderByDesc(MyEvent::getCreateTime);

        Long current = reqVO != null && reqVO.getCurrent() != null ? reqVO.getCurrent() : 1L;
        Long size = reqVO != null && reqVO.getSize() != null ? reqVO.getSize() : 10L;

        Page<MyEvent> page = new Page<>(current, size);
        Page<MyEvent> eventPage = myEventMapper.selectPage(page, wrapper);

        List<EventRespVO> voList = eventPage.getRecords().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, eventPage.getTotal(), current, size);
    }

    @Override
    public List<MyEvent> getEventsToPush() {
        LocalDate today = LocalDate.now();

        List<MyEvent> allEvents = myEventMapper.selectList(
                new LambdaQueryWrapper<MyEvent>()
                        .eq(MyEvent::getPushStatus, 0)
                        .eq(MyEvent::getDeleted, 0)
        );

        List<MyEvent> eventsToPush = new ArrayList<>();
        for (MyEvent event : allEvents) {
            if (event.getAdvanceDays() == null) {
                event.setAdvanceDays(3);
            }
            LocalDate remindDate = event.getEventDate().minusDays(event.getAdvanceDays());
            if (!today.isBefore(remindDate) && !today.isAfter(event.getEventDate())) {
                eventsToPush.add(event);
            }
        }

        return eventsToPush;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsPushed(Long id) {
        MyEvent event = myEventMapper.selectById(id);
        if (event != null && event.getPushStatus() == 0) {
            event.setPushStatus(1);
            event.setPushTime(LocalDateTime.now());
            myEventMapper.updateById(event);
        }
    }

    private EventRespVO convertToRespVO(MyEvent event) {
        return EventRespVO.builder()
                .id(event.getId())
                .userId(event.getUserId())
                .title(event.getTitle())
                .eventType(event.getEventType())
                .eventDate(event.getEventDate())
                .location(event.getLocation())
                .remark(event.getRemark())
                .advanceDays(event.getAdvanceDays())
                .status(event.getPushStatus())
                .createTime(event.getCreateTime())
                .build();
    }
}
