package com.bookkeeping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("my_event")
@Schema(description = "我的事件实体")
public class MyEvent {

    @TableId(type = IdType.ASSIGN_ID)
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

    @Schema(description = "提前提醒天数：默认3天")
    private Integer advanceDays;

    @Schema(description = "推送状态：0-未推送，1-已推送")
    private Integer pushStatus;

    @Schema(description = "推送时间")
    private LocalDateTime pushTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    private Integer deleted;
}
