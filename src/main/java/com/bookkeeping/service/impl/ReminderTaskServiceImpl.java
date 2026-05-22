package com.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.dto.ReminderTaskDTO;
import com.bookkeeping.entity.ReminderTask;
import com.bookkeeping.entity.User;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.mapper.ReminderTaskMapper;
import com.bookkeeping.mapper.UserMapper;
import com.bookkeeping.service.ContactService;
import com.bookkeeping.service.ReminderTaskService;
import com.bookkeeping.vo.ContactVO;
import com.bookkeeping.vo.ReminderTaskVO;
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
    public ReminderTaskVO createTask(Long userId, ReminderTaskDTO dto) {
        ReminderTask task = new ReminderTask();
        BeanUtils.copyProperties(dto, task);
        task.setUserId(userId);
        task.setStatus(0);

        if (dto.getContactIds() != null && !dto.getContactIds().isEmpty()) {
            try {
                task.setContactIds(objectMapper.writeValueAsString(dto.getContactIds()));
            } catch (Exception e) {
                log.error("序列化contactIds失败", e);
            }
        }

        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            try {
                task.setCategoryIds(objectMapper.writeValueAsString(dto.getCategoryIds()));
            } catch (Exception e) {
                log.error("序列化categoryIds失败", e);
            }
        }

        reminderTaskMapper.insert(task);
        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReminderTaskVO updateTask(Long userId, Long id, ReminderTaskDTO dto) {
        ReminderTask task = reminderTaskMapper.selectById(id);
        if (task == null || task.getDeleted() == 1) {
            throw new BusinessException("任务不存在");
        }
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该任务");
        }
        if (task.getStatus() != 0) {
            throw new BusinessException("只有待发送的任务才能修改");
        }

        BeanUtils.copyProperties(dto, task);

        if (dto.getContactIds() != null && !dto.getContactIds().isEmpty()) {
            try {
                task.setContactIds(objectMapper.writeValueAsString(dto.getContactIds()));
            } catch (Exception e) {
                log.error("序列化contactIds失败", e);
            }
        }

        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            try {
                task.setCategoryIds(objectMapper.writeValueAsString(dto.getCategoryIds()));
            } catch (Exception e) {
                log.error("序列化categoryIds失败", e);
            }
        }

        reminderTaskMapper.updateById(task);
        return convertToVO(task);
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
    public ReminderTaskVO getTaskDetail(Long userId, Long id) {
        ReminderTask task = reminderTaskMapper.selectById(id);
        if (task == null || task.getDeleted() == 1 || !task.getUserId().equals(userId)) {
            throw new BusinessException("任务不存在");
        }
        return convertToVO(task);
    }

    @Override
    public Page<ReminderTaskVO> getTaskList(Long userId, Integer status, Long current, Long size) {
        LambdaQueryWrapper<ReminderTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReminderTask::getUserId, userId);
        wrapper.eq(ReminderTask::getDeleted, 0);

        if (status != null) {
            wrapper.eq(ReminderTask::getStatus, status);
        }

        wrapper.orderByDesc(ReminderTask::getRemindDate);
        wrapper.orderByDesc(ReminderTask::getCreateTime);

        Page<ReminderTask> page = new Page<>(current, size);
        Page<ReminderTask> taskPage = reminderTaskMapper.selectPage(page, wrapper);

        Page<ReminderTaskVO> resultPage = new Page<>(taskPage.getCurrent(), taskPage.getSize(), taskPage.getTotal());
        List<ReminderTaskVO> voList = taskPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        resultPage.setRecords(voList);
        return resultPage;
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
    public List<ReminderTaskVO> getPendingTasks() {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<ReminderTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReminderTask::getStatus, 0);
        wrapper.eq(ReminderTask::getDeleted, 0);
        wrapper.le(ReminderTask::getRemindDate, today);
        wrapper.orderByAsc(ReminderTask::getRemindDate);

        List<ReminderTask> tasks = reminderTaskMapper.selectList(wrapper);
        return tasks.stream().map(this::convertToVO).collect(Collectors.toList());
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

    private ReminderTaskVO convertToVO(ReminderTask task) {
        ReminderTaskVO vo = new ReminderTaskVO();
        BeanUtils.copyProperties(task, vo);

        if (StringUtils.hasText(task.getContactIds())) {
            try {
                List<Long> contactIds = objectMapper.readValue(task.getContactIds(), new TypeReference<List<Long>>() {});
                vo.setContactIds(contactIds);
                List<ContactVO> contacts = contactService.getContactsByIds(contactIds);
                vo.setContacts(contacts);
            } catch (Exception e) {
                log.error("反序列化contactIds失败", e);
            }
        }

        if (StringUtils.hasText(task.getCategoryIds())) {
            try {
                List<Long> categoryIds = objectMapper.readValue(task.getCategoryIds(), new TypeReference<List<Long>>() {});
                vo.setCategoryIds(categoryIds);
            } catch (Exception e) {
                log.error("反序列化categoryIds失败", e);
            }
        }

        return vo;
    }
}
