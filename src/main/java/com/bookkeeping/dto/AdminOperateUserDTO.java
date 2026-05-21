package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员操作用户DTO")
public class AdminOperateUserDTO {

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "角色：1-普通用户，2-管理员")
    private Integer role;
}
