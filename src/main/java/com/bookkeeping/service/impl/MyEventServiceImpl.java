package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.dto.MyEventDTO;
import com.bookkeeping.entity.Contact;
import com.bookkeeping.entity.MyEvent;
import com.bookkeeping.entity.Record;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.ContactMapper;
import com.bookkeeping.mapper.MyEventMapper;
import com.bookkeeping.mapper.RecordMapper;
import com.bookkeeping.service.MyEventService;
import com.bookkeeping.vo.ContactVO;
import com.bookkeeping.vo.MyEventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    public MyEventVO createEvent(Long userId, MyEventDTO dto) {
        MyEvent event = new MyEvent();
        BeanUtils.copyProperties(dto, event);
        event.setUserId(userId);
        event.setPushStatus(0);
        if (dto.getAdvanceDays() == null) {
            event.setAdvanceDays(3);
        }
        myEventMapper.insert(event);
        return convertToVO(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MyEventVO updateEvent(Long userId, Long id, MyEventDTO dto) {
        MyEvent event = myEventMapper.selectById(id);
        if (event == null || event.getDeleted() == 1) {
            throw new BusinessException("事件不存在");
        }
        if (!event.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该事件");
        }

        BeanUtils.copyProperties(dto, event);
        if (dto.getAdvanceDays() == null) {
            event.setAdvanceDays(3);
        }
        myEventMapper.updateById(event);
        return convertToVO(event);
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
    public MyEventVO getEventDetail(Long userId, Long id) {
        MyEvent event = myEventMapper.selectById(id);
        if (event == null || event.getDeleted() == 1 || !event.getUserId().equals(userId)) {
            throw new BusinessException("事件不存在");
        }
        return convertToVO(event);
    }

    @Override
    public Page<MyEventVO> getEventList(Long userId, Integer status, Long current, Long size) {
        LambdaQueryWrapper<MyEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MyEvent::getUserId, userId);
        wrapper.eq(MyEvent::getDeleted, 0);
        if (status != null) {
            wrapper.eq(MyEvent::getPushStatus, status);
        }
        wrapper.orderByDesc(MyEvent::getEventDate);
        wrapper.orderByDesc(MyEvent::getCreateTime);

        Page<MyEvent> page = new Page<>(current, size);
        Page<MyEvent> eventPage = myEventMapper.selectPage(page, wrapper);

        Page<MyEventVO> resultPage = new Page<>(eventPage.getCurrent(), eventPage.getSize(), eventPage.getTotal());
        List<MyEventVO> voList = eventPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public List<MyEventVO> getEventsToPush() {
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

        return eventsToPush.stream().map(this::convertToVO).collect(Collectors.toList());
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

    private MyEventVO convertToVO(MyEvent event) {
        MyEventVO vo = new MyEventVO();
        BeanUtils.copyProperties(event, vo);

        List<ContactVO> contacts = getContactsWhoReceivedGifts(event.getUserId());
        vo.setContacts(contacts);

        return vo;
    }

    private List<ContactVO> getContactsWhoReceivedGifts(Long userId) {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getUserId, userId);
        wrapper.eq(Record::getType, 2);
        wrapper.eq(Record::getDeleted, 0);
        wrapper.isNotNull(Record::getContactId);

        List<Record> records = recordMapper.selectList(wrapper);

        Set<Long> contactIds = new HashSet<>();
        for (Record record : records) {
            if (record.getContactId() != null) {
                contactIds.add(record.getContactId());
            }
        }

        if (contactIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Contact> contacts = contactMapper.selectBatchIds(contactIds);
        return contacts.stream().map(contact -> {
            ContactVO vo = new ContactVO();
            BeanUtils.copyProperties(contact, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
