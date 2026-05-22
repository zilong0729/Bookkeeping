package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "统一请求基类")
public class BaseReqVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "关键词搜索")
    private String keyword;
}
