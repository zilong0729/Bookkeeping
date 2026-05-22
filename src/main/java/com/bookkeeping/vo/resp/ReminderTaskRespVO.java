package com.bookkeeping.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提醒任务响应VO")
public class ReminderTaskRespVO {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "提醒标题")
    private String title;

    @Schema(description = "提醒内容")
    private String content;

    @Schema(description = "提醒类型：1-婚宴，2-生日，3-乔迁，4-其他")
    private Integer remindType;

    @Schema(description = "提醒日期")
    private LocalDate remindDate;

    @Schema(description = "状态：0-待发送，1-已发送，2-已取消")
    private Integer status;

    @Schema(description = "关联的联系人列表")
    private List<ContactRespVO> contacts;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
