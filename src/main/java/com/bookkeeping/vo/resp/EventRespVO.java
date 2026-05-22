package com.bookkeeping.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "事件响应VO")
public class EventRespVO {

    @Schema(description = "事件ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "事件标题")
    private String title;

    @Schema(description = "事件类型：1-结婚、2-满月酒、3-生日、4-升学、5-乔迁、6-其他")
    private Integer eventType;

    @Schema(description = "事件类型名称")
    private String eventTypeName;

    @Schema(description = "事件日期")
    private LocalDate eventDate;

    @Schema(description = "事件地点")
    private String location;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "提前提醒天数")
    private Integer advanceDays;

    @Schema(description = "状态：0-未开始、1-即将到来、2-已过期")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
