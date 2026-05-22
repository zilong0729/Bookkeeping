package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "联系人列表查询请求VO")
public class ContactListReqVO extends PageReqVO {

    @Schema(description = "关系类型")
    private String relation;
}
