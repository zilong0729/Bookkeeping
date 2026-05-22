package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新事件请求VO")
public class UpdateEventReqVO {

    @Schema(description = "事件ID", required = true)
    private Long id;

    @Schema(description = "事件标题")
    private String title;

    @Schema(description = "事件类型：1-结婚、2-满月酒、3-生日、4-升学、5-乔迁、6-其他")
    private Integer eventType;

    @Schema(description = "事件日期")
    private java.time.LocalDate eventDate;

    @Schema(description = "事件地点")
    private String location;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "提前提醒天数")
    private Integer advanceDays;
}
