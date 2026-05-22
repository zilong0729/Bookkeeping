package com.bookkeeping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Schema(description = "提醒任务VO")
public class ReminderTaskVO {
    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "提醒标题")
    private String title;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "提醒类型：1-婚宴，2-生日，3-乔迁，4-其他")
    private Integer remindType;

    @Schema(description = "提醒日期")
    private LocalDate remindDate;

    @Schema(description = "提醒时间")
    private LocalTime remindTime;

    @Schema(description = "关联的联系人ID列表")
    private List<Long> contactIds;

    @Schema(description = "关联的账单类别ID列表")
    private List<Long> categoryIds;

    @Schema(description = "分享模板")
    private String shareTemplate;

    @Schema(description = "状态：0-待发送，1-已发送，2-已取消")
    private Integer status;

    @Schema(description = "实际发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "关联的联系人列表")
    private List<ContactVO> contacts;
}
