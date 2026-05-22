package com.bookkeeping.vo.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应VO")
public class LoginRespVO {

    @Schema(description = "访问Token")
    private String token;

    @Schema(description = "Token过期时间（秒）")
    private Long expiresIn;

    @Schema(description = "用户信息")
    private UserRespVO userInfo;
}
