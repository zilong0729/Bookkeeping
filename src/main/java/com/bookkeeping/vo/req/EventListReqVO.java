package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "事件列表查询请求VO")
public class EventListReqVO extends PageReqVO {

    @Schema(description = "状态：0-未开始、1-即将到来、2-已过期、3-全部")
    private Integer status;
}
