package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员操作用户DTO
 */
@Data
@Schema(description = "管理员操作用户请求参数")
public class AdminOperateUserDTO {

    @Schema(description = "要设置的状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "要设置的角色：0-普通用户，1-管理员")
    private Integer role;
}
