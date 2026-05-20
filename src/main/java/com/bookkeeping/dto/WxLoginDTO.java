package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录DTO
 */
@Data
@Schema(description = "微信登录请求参数")
public class WxLoginDTO {

    @NotBlank(message = "微信登录code不能为空")
    @Schema(description = "微信登录临时code", required = true)
    private String code;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatarUrl;
}
