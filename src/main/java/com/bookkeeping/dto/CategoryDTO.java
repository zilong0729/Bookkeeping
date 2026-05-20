package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 类别DTO
 */
@Data
@Schema(description = "类别请求参数")
public class CategoryDTO {

    @NotBlank(message = "类别名称不能为空")
    @Schema(description = "类别名称", required = true)
    private String name;

    @NotNull(message = "类型不能为空")
    @Schema(description = "类型：1-收入，2-支出", required = true)
    private Integer type;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sortOrder;
}
