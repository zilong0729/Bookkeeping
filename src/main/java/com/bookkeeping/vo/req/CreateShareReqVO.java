package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建分享请求VO")
public class CreateShareReqVO {

    @Schema(description = "分享类型：1-事件分享、2-账单分享、3-邀请注册", required = true)
    @NotNull(message = "分享类型不能为空")
    private Integer shareType;

    @Schema(description = "关联ID（事件ID或账单ID）")
    private Long relatedId;

    @Schema(description = "关系类型：friend/family/colleague/other")
    private String relation;
}
