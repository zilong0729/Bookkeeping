package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "我的事件请求DTO")
public class MyEventDTO {

    @Schema(description = "事件标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "事件类型：1-婚宴，2-生日，3-乔迁，4-其他")
    @NotNull(message = "事件类型不能为空")
    private Integer eventType;

    @Schema(description = "事件日期")
    @NotNull(message = "事件日期不能为空")
    private LocalDate eventDate;

    @Schema(description = "地点")
    private String location;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "提前提醒天数：默认3天")
    private Integer advanceDays;
}
