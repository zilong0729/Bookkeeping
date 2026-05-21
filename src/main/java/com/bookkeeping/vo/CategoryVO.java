package com.bookkeeping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "类别信息VO")
public class CategoryVO {

    @Schema(description = "类别ID")
    private Long id;

    @Schema(description = "类别名称")
    private String name;

    @Schema(description = "类型：1-支出，2-收入")
    private Integer type;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否默认：0-否，1-是")
    private Integer isDefault;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
