package com.bookkeeping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "联系人VO")
public class ContactVO {
    @Schema(description = "联系人ID")
    private Long id;

    @Schema(description = "联系人姓名")
    private String name;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "微信OpenID")
    private String openId;

    @Schema(description = "关系（亲戚/朋友/同事等）")
    private String relation;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
