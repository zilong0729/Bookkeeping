package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新类别请求VO")
public class UpdateCategoryReqVO {

    @Schema(description = "类别ID", required = true)
    private Long id;

    @Schema(description = "类别名称")
    private String name;

    @Schema(description = "类型：1-支出，2-收入")
    private Integer type;

    @Schema(description = "图标")
    private String icon;
}
