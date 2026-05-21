package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员查询用户DTO")
public class AdminUserQueryDTO {

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "角色：1-普通用户，2-管理员")
    private Integer role;

    @Schema(description = "当前页")
    private Long current = 1L;

    @Schema(description = "每页条数")
    private Long size = 10L;
}
