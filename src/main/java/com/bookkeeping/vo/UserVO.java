package com.bookkeeping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Data
@Schema(description = "用户信息")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "角色：0-普通用户，1-管理员")
    private Integer role;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
