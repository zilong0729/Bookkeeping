package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "创建事件请求VO")
public class CreateEventReqVO extends BaseReqVO {

    @Schema(description = "事件标题", required = true)
    private String title;

    @Schema(description = "事件类型：1-结婚、2-满月酒、3-生日、4-升学、5-乔迁、6-其他", required = true)
    private Integer eventType;

    @Schema(description = "事件日期", required = true)
    private LocalDate eventDate;

    @Schema(description = "事件地点")
    private String location;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "提前提醒天数")
    private Integer advanceDays = 3;
}
