package com.bookkeeping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookkeeping.dto.ReminderTaskDTO;
import com.bookkeeping.vo.ReminderTaskVO;

import java.util.List;

public interface ReminderTaskService {
    ReminderTaskVO createTask(Long userId, ReminderTaskDTO dto);

    ReminderTaskVO updateTask(Long userId, Long id, ReminderTaskDTO dto);

    void deleteTask(Long userId, Long id);

    ReminderTaskVO getTaskDetail(Long userId, Long id);

    Page<ReminderTaskVO> getTaskList(Long userId, Integer status, Long current, Long size);

    void cancelTask(Long userId, Long id);

    List<ReminderTaskVO> getPendingTasks();

    void markTaskAsSent(Long id);

    String generateShareParams(Long userId, Long taskId, Long contactId);
}
