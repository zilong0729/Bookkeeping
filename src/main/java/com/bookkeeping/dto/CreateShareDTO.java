package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建分享邀请DTO")
public class CreateShareDTO {

    @Schema(description = "分享类型：1-事件邀请，2-好友邀请", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分享类型不能为空")
    private Integer shareType;

    @Schema(description = "关联ID（事件ID等）")
    private Long relatedId;

    @Schema(description = "关系描述（可选）")
    private String relation;
}
