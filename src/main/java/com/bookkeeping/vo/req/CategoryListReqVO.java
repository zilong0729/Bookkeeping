package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "类别列表查询请求VO")
public class CategoryListReqVO extends BaseReqVO {

    @Schema(description = "类型：1-支出，2-收入")
    private Integer type;
}
