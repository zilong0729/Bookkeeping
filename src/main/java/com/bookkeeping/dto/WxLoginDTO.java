package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "微信登录DTO")
public class WxLoginDTO {

    @Schema(description = "微信登录code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "微信登录code不能为空")
    private String code;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatarUrl;
}
