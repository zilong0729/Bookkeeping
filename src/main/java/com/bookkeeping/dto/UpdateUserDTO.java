package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户信息DTO
 */
@Data
@Schema(description = "更新用户信息请求参数")
public class UpdateUserDTO {

    @Size(max = 100, message = "昵称长度不能超过100")
    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Size(max = 20, message = "手机号长度不能超过20")
    @Schema(description = "手机号")
    private String phone;
}
