package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新用户信息DTO")
public class UpdateUserDTO {

    @Schema(description = "昵称")
    @Size(max = 100, message = "昵称长度不能超过100")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatarUrl;

    @Schema(description = "手机号")
    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;
}
