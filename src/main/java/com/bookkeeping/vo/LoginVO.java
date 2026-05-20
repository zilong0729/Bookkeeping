package com.bookkeeping.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录返回VO
 */
@Data
@Schema(description = "登录返回结果")
public class LoginVO {

    @Schema(description = "用户token")
    private String token;

    @Schema(description = "token过期时间（秒）")
    private Long expiresIn;

    @Schema(description = "用户信息")
    private UserVO userInfo;
}
