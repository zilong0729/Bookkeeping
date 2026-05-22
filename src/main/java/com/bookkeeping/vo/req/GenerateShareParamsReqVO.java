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
@Schema(description = "生成分享参数请求VO")
public class GenerateShareParamsReqVO {

    @Schema(description = "任务ID", required = true)
    private Long taskId;

    @Schema(description = "联系人ID")
    private Long contactId;
}
