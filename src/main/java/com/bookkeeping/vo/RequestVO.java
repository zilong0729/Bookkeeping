package com.bookkeeping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "统一请求参数VO")
public class RequestVO {

    @Schema(description = "ID（通用）")
    private Long id;

    @Schema(description = "关键词搜索")
    private String keyword;

    @Schema(description = "当前页码")
    private Long current = 1L;

    @Schema(description = "每页条数")
    private Long size = 10L;

    @Schema(description = "类型：1-支出，2-收入")
    private Integer type;

    @Schema(description = "类别ID")
    private Long categoryId;

    @Schema(description = "联系人ID")
    private Long contactId;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "分享类型：1-事件邀请，2-好友邀请")
    private Integer shareType;

    @Schema(description = "关联ID")
    private Long relatedId;

    @Schema(description = "分享码")
    private String shareCode;

    @Schema(description = "ID列表")
    private List<Long> ids;

    @Schema(description = "类别ID列表")
    private List<Long> categoryIds;

    @Schema(description = "联系人ID列表")
    private List<Long> contactIds;
}
