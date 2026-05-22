package com.bookkeeping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "我的事件VO")
public class MyEventVO {

    @Schema(description = "事件ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "事件标题")
    private String title;

    @Schema(description = "事件类型：1-婚宴，2-生日，3-乔迁，4-其他")
    private Integer eventType;

    @Schema(description = "事件日期")
    private LocalDate eventDate;

    @Schema(description = "地点")
    private String location;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "提前提醒天数")
    private Integer advanceDays;

    @Schema(description = "推送状态：0-未推送，1-已推送")
    private Integer pushStatus;

    @Schema(description = "推送时间")
    private LocalDateTime pushTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "需要提醒的联系人列表")
    private List<ContactVO> contacts;


}
