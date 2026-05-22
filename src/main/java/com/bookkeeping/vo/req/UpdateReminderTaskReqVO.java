package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新提醒任务请求VO")
public class UpdateReminderTaskReqVO {

    @Schema(description = "任务ID", required = true)
    @NotNull(message = "任务ID不能为空")
    private Long id;

    @Schema(description = "提醒标题")
    private String title;

    @Schema(description = "提醒类型：1-婚宴，2-生日，3-乔迁，4-其他")
    private Integer remindType;

    @Schema(description = "提醒日期")
    private LocalDate remindDate;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "关联的联系人ID列表")
    private java.util.List<Long> contactIds;
}
