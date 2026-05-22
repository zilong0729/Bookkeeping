package com.bookkeeping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "统一响应参数VO")
public class ResponseVO {

    @Schema(description = "响应状态码")
    private Integer code;

    @Schema(description = "响应消息")
    private String message;

    @Schema(description = "数据对象（通用）")
    private Object data;

    @Schema(description = "数据列表（通用）")
    private List<?> list;

    @Schema(description = "分页信息")
    private PageInfo page;

    @Schema(description = "用户信息")
    private UserVO userInfo;

    @Schema(description = "Token信息")
    private TokenInfo tokenInfo;

    @Schema(description = "统计数据")
    private StatisticsInfo statistics;

    @Schema(description = "分享信息")
    private ShareInfo shareInfo;

    @Data
    @Schema(description = "分页信息")
    public static class PageInfo {
        @Schema(description = "总记录数")
        private Long total;

        @Schema(description = "每页条数")
        private Long size;

        @Schema(description = "当前页码")
        private Long current;

        @Schema(description = "总页数")
        private Long pages;

        @Schema(description = "是否有上一页")
        private Boolean hasPrevious;

        @Schema(description = "是否有下一页")
        private Boolean hasNext;
    }

    @Data
    @Schema(description = "Token信息")
    public static class TokenInfo {
        @Schema(description = "访问Token")
        private String token;

        @Schema(description = "过期时间（秒）")
        private Long expiresIn;

        @Schema(description = "Token类型")
        private String tokenType = "Bearer";
    }

    @Data
    @Schema(description = "统计数据")
    public static class StatisticsInfo {
        @Schema(description = "总收入")
        private String totalIncome;

        @Schema(description = "总支出")
        private String totalExpense;

        @Schema(description = "结余")
        private String balance;
    }

    @Data
    @Schema(description = "分享信息")
    public static class ShareInfo {
        @Schema(description = "分享ID")
        private Long id;

        @Schema(description = "分享码")
        private String shareCode;

        @Schema(description = "分享链接")
        private String shareUrl;

        @Schema(description = "分享类型")
        private Integer shareType;

        @Schema(description = "状态")
        private Integer status;

        @Schema(description = "过期时间")
        private LocalDateTime expireTime;

        @Schema(description = "分享者信息")
        private UserVO shareUser;

        @Schema(description = "关联事件")
        private MyEventVO event;
    }

    public static ResponseVO success(Object data) {
        ResponseVO response = new ResponseVO();
        response.setCode(200);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }

    public static ResponseVO success(String message, Object data) {
        ResponseVO response = new ResponseVO();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static ResponseVO error(String message) {
        ResponseVO response = new ResponseVO();
        response.setCode(500);
        response.setMessage(message);
        return response;
    }

    public static ResponseVO error(Integer code, String message) {
        ResponseVO response = new ResponseVO();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
