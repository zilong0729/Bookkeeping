package com.bookkeeping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String username;

    private String operation;

    private String method;

    private String requestUrl;

    private String requestMethod;

    private String requestParams;

    private String responseResult;

    private String ip;

    /** 操作状态：0-失败，1-成功 */
    private Integer status;

    private String errorMsg;

    /** 执行耗时（毫秒） */
    private Long executionTime;

    private LocalDateTime createTime;
}
