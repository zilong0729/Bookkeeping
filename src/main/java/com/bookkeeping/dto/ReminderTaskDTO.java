package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Schema(description = "提醒任务请求DTO")
public class ReminderTaskDTO {
    @Schema(description = "提醒标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "提醒类型：1-婚宴，2-生日，3-乔迁，4-其他")
    @NotNull(message = "提醒类型不能为空")
    private Integer remindType;

    @Schema(description = "提醒日期")
    @NotNull(message = "提醒日期不能为空")
    private LocalDate remindDate;

    @Schema(description = "提醒时间")
    private LocalTime remindTime;

    @Schema(description = "关联的联系人ID列表")
    private List<Long> contactIds;

    @Schema(description = "关联的账单类别ID列表")
    private List<Long> categoryIds;

    @Schema(description = "分享模板")
    private String shareTemplate;
}
