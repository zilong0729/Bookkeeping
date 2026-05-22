package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.entity.ReminderTask;
import com.bookkeeping.entity.User;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.ReminderTaskMapper;
import com.bookkeeping.mapper.UserMapper;
import com.bookkeeping.service.ContactService;
import com.bookkeeping.service.ReminderTaskService;
import com.bookkeeping.vo.req.*;
import com.bookkeeping.vo.resp.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderTaskServiceImpl implements ReminderTaskService {

    private final ReminderTaskMapper reminderTaskMapper;
    private final ContactService contactService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReminderTaskRespVO createTask(Long userId, CreateReminderTaskReqVO reqVO) {
        ReminderTask task = new ReminderTask();
        BeanUtils.copyProperties(reqVO, task);
        task.setUserId(userId);
        task.setStatus(0);

        if (reqVO.getContactIds() != null && !reqVO.getContactIds().isEmpty()) {
            try {
                task.setContactIds(objectMapper.writeValueAsString(reqVO.getContactIds()));
            } catch (Exception e) {
                log.error("序列化contactIds失败", e);
            }
        }

        reminderTaskMapper.insert(task);
        return convertToRespVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReminderTaskRespVO updateTask(Long userId, UpdateReminderTaskReqVO reqVO) {
        ReminderTask task = reminderTaskMapper.selectById(reqVO.getId());
        if (task == null || task.getDeleted() == 1) {
            throw new BusinessException("任务不存在");
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该任务");
        }
        if (task.getStatus() != 0) {
            throw new BusinessException("只有待发送的任务才能修改");
        }

        BeanUtils.copyProperties(reqVO, task);

        if (reqVO.getContactIds() != null && !reqVO.getContactIds().isEmpty()) {
            try {
                task.setContactIds(objectMapper.writeValueAsString(reqVO.getContactIds()));
            } catch (Exception e) {
                log.error("序列化contactIds失败", e);
            }
        }

        reminderTaskMapper.updateById(task);
        return convertToRespVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long userId, Long id) {
        ReminderTask task = reminderTaskMapper.selectById(id);
        if (task == null || task.getDeleted() == 1) {
            throw new BusinessException("任务不存在");
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该任务");
        }
        reminderTaskMapper.deleteById(id);
    }

    @Override
    public ReminderTaskRespVO getTaskDetail(Long userId, Long id) {
        ReminderTask task = reminderTaskMapper.selectById(id);
        if (task == null || task.getDeleted() == 1 || !task.getUserId().equals(userId)) {
            throw new BusinessException("任务不存在");
        }
        return convertToRespVO(task);
    }

    @Override
    public PageResult<ReminderTaskRespVO> getTaskList(Long userId, ReminderTaskListReqVO reqVO) {
        LambdaQueryWrapper<ReminderTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReminderTask::getUserId, userId);
        wrapper.eq(ReminderTask::getDeleted, 0);

        if (reqVO.getStatus() != null) {
            wrapper.eq(ReminderTask::getStatus, reqVO.getStatus());
        }

        wrapper.orderByDesc(ReminderTask::getRemindDate);
        wrapper.orderByDesc(ReminderTask::getCreateTime);

        Long current = reqVO.getCurrent() != null ? reqVO.getCurrent() : 1L;
        Long size = reqVO.getSize() != null ? reqVO.getSize() : 10L;
        
        Page<ReminderTask> page = new Page<>(current, size);
        Page<ReminderTask> taskPage = reminderTaskMapper.selectPage(page, wrapper);

        List<ReminderTaskRespVO> voList = taskPage.getRecords().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, taskPage.getTotal(), taskPage.getCurrent(), taskPage.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long userId, Long id) {
        ReminderTask task = reminderTaskMapper.selectById(id);
        if (task == null || task.getDeleted() == 1) {
            throw new BusinessException("任务不存在");
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该任务");
        }
        if (task.getStatus() != 0) {
            throw new BusinessException("只有待发送的任务才能取消");
        }

        task.setStatus(2);
        reminderTaskMapper.updateById(task);
    }

    @Override
    public List<ReminderTask> getPendingTasks() {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<ReminderTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReminderTask::getStatus, 0);
        wrapper.eq(ReminderTask::getDeleted, 0);
        wrapper.le(ReminderTask::getRemindDate, today);
        wrapper.orderByAsc(ReminderTask::getRemindDate);

        return reminderTaskMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markTaskAsSent(Long id) {
        ReminderTask task = reminderTaskMapper.selectById(id);
        if (task != null && task.getStatus() == 0) {
            task.setStatus(1);
            task.setSendTime(LocalDateTime.now());
            reminderTaskMapper.updateById(task);
        }
    }

    @Override
    public String generateShareParams(Long userId, Long taskId, Long contactId) {
        ReminderTask task = reminderTaskMapper.selectById(taskId);
        if (task == null || task.getDeleted() == 1 || !task.getUserId().equals(userId)) {
            throw new BusinessException("任务不存在");
        }

        User user = userMapper.selectById(userId);

        String shareId = UUID.randomUUID().toString().replace("-", "");

        StringBuilder sb = new StringBuilder();
        sb.append("shareId=").append(shareId);
        sb.append("&taskId=").append(taskId);
        if (contactId != null) {
            sb.append("&contactId=").append(contactId);
        }
        if (user != null && user.getNickname() != null) {
            sb.append("&userName=").append(user.getNickname());
        }

        return sb.toString();
    }

    private ReminderTaskRespVO convertToRespVO(ReminderTask task) {
        ReminderTaskRespVO respVO = new ReminderTaskRespVO();
        BeanUtils.copyProperties(task, respVO);

        if (StringUtils.hasText(task.getContactIds())) {
            try {
                List<Long> contactIds = objectMapper.readValue(task.getContactIds(), new TypeReference<List<Long>>() {});
                List<ContactRespVO> contacts = contactService.getContactsByIds(contactIds);
                respVO.setContacts(contacts);
            } catch (Exception e) {
                log.error("反序列化contactIds失败", e);
            }
        }

        return respVO;
    }
}
