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
@Schema(description = "查询提醒任务列表请求VO")
public class ReminderTaskListReqVO {

    @Schema(description = "状态：0-待发送，1-已发送，2-已取消")
    private Integer status;

    @Schema(description = "当前页码")
    private Long current = 1L;

    @Schema(description = "每页条数")
    private Long size = 10L;
}
