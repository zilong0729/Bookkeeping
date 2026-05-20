package com.bookkeeping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志VO
 */
@Data
@Schema(description = "操作日志信息")
public class OperationLogVO {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "操作用户ID")
    private Long userId;

    @Schema(description = "操作用户名")
    private String username;

    @Schema(description = "操作描述")
    private String operation;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "HTTP请求方法")
    private String requestMethod;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "返回结果")
    private String responseResult;

    @Schema(description = "操作IP")
    private String ip;

    @Schema(description = "操作状态：0-失败，1-成功")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "执行耗时（毫秒）")
    private Long executionTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
