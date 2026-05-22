package com.bookkeeping.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "操作日志响应VO")
public class OperationLogRespVO {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人用户名")
    private String operatorName;

    @Schema(description = "操作描述")
    private String operation;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "请求方法类型")
    private String requestMethod;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "响应结果")
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
    private String createTime;
}
