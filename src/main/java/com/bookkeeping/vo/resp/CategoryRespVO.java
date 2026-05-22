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
@Schema(description = "类别响应VO")
public class CategoryRespVO {

    @Schema(description = "类别ID")
    private Long id;

    @Schema(description = "类别名称")
    private String name;

    @Schema(description = "类型：1-支出，2-收入")
    private Integer type;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private String createTime;
}
