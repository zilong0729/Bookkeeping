package com.bookkeeping.service.impl;

import com.bookkeeping.entity.OperationLog;
import com.bookkeeping.mapper.OperationLogMapper;
import com.bookkeeping.service.AsyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步日志服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncLogServiceImpl implements AsyncLogService {

    private final OperationLogMapper operationLogMapper;

    @Override
    @Async("taskExecutor")
    public void saveLog(OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("异步保存操作日志失败", e);
        }
    }
}
