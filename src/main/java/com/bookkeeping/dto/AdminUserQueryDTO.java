package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员用户查询DTO
 */
@Data
@Schema(description = "管理员用户查询参数")
public class AdminUserQueryDTO {

    @Schema(description = "用户昵称（模糊查询）")
    private String nickname;

    @Schema(description = "手机号（模糊查询）")
    private String phone;

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "角色：0-普通用户，1-管理员")
    private Integer role;

    @Schema(description = "页码，默认1")
    private Long current = 1L;

    @Schema(description = "每页大小，默认10")
    private Long size = 10L;
}
