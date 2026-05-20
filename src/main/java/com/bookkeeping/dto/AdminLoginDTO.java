package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录DTO
 */
@Data
@Schema(description = "管理员登录请求参数")
public class AdminLoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "管理员用户名", required = true)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "管理员密码", required = true)
    private String password;
}
