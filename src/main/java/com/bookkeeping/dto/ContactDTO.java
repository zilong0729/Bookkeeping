package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "联系人请求DTO")
public class ContactDTO {
    @Schema(description = "联系人姓名")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "微信OpenID")
    private String openId;

    @Schema(description = "关系（亲戚/朋友/同事等）")
    private String relation;

    @Schema(description = "备注")
    private String remark;
}
