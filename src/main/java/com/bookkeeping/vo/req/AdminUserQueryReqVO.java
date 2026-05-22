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
@Schema(description = "管理员查询用户列表请求VO")
public class AdminUserQueryReqVO {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "当前页码")
    private Long current = 1L;

    @Schema(description = "每页条数")
    private Long size = 10L;
}
