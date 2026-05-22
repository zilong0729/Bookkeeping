package com.bookkeeping.service;

import com.bookkeeping.entity.ReminderTask;
import com.bookkeeping.vo.req.*;
import com.bookkeeping.vo.resp.*;

import java.util.List;

public interface ReminderTaskService {
    ReminderTaskRespVO createTask(Long userId, CreateReminderTaskReqVO reqVO);

    ReminderTaskRespVO updateTask(Long userId, UpdateReminderTaskReqVO reqVO);

    void deleteTask(Long userId, Long id);

    ReminderTaskRespVO getTaskDetail(Long userId, Long id);

    PageResult<ReminderTaskRespVO> getTaskList(Long userId, ReminderTaskListReqVO reqVO);

    void cancelTask(Long userId, Long id);

    List<ReminderTask> getPendingTasks();

    void markTaskAsSent(Long id);

    String generateShareParams(Long userId, Long taskId, Long contactId);
}
