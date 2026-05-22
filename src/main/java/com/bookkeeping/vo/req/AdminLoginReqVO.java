package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员登录请求VO")
public class AdminLoginReqVO {

    @Schema(description = "管理员用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "管理员密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}
