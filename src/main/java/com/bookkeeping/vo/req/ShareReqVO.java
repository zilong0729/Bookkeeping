package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分享请求VO")
public class ShareReqVO extends BaseReqVO {

    @Schema(description = "分享类型：1-事件分享、2-账单分享、3-邀请注册", required = true)
    private Integer shareType;

    @Schema(description = "关联ID（事件ID或账单ID）")
    private Long relatedId;

    @Schema(description = "关系类型：friend/family/colleague/other")
    private String relation;

    @Schema(description = "分享码（用于通过分享码接受邀请）")
    private String shareCode;
}
