package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "创建联系人请求VO")
public class CreateContactReqVO extends BaseReqVO {

    @Schema(description = "联系人姓名", required = true)
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
