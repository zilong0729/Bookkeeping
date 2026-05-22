package com.bookkeeping.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "联系人响应VO")
public class ContactRespVO {

    @Schema(description = "联系人ID")
    private Long id;

    @Schema(description = "联系人姓名")
    private String name;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "微信openId")
    private String openId;

    @Schema(description = "关系类型")
    private String relation;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private String createTime;
}
