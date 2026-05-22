package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建类别请求VO")
public class CreateCategoryReqVO {

    @Schema(description = "类别名称", required = true)
    @NotBlank(message = "类别名称不能为空")
    private String name;

    @Schema(description = "类型：1-支出，2-收入", required = true)
    @NotNull(message = "类型不能为空")
    private Integer type;

    @Schema(description = "图标")
    private String icon;
}
