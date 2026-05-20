package com.bookkeeping.service;

import com.bookkeeping.entity.OperationLog;

/**
 * 异步日志服务接口
 */
public interface AsyncLogService {

    /**
     * 异步保存操作日志
     */
    void saveLog(OperationLog operationLog);
}
