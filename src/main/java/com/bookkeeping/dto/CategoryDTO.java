package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "类别DTO")
public class CategoryDTO {

    @Schema(description = "类别名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "类别名称不能为空")
    private String name;

    @Schema(description = "类型：1-支出，2-收入", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "类型不能为空")
    private Integer type;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sortOrder;
}
