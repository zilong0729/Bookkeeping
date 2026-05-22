package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新联系人请求VO")
public class UpdateContactReqVO {

    @Schema(description = "联系人ID", required = true)
    private Long id;

    @Schema(description = "联系人姓名")
    private String name;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "微信openId")
    private String openId;

    @Schema(description = "关系类型：friend/family/colleague/other")
    private String relation;

    @Schema(description = "备注")
    private String remark;
}
