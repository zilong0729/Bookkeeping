package com.bookkeeping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO（包含设备信息）
 */
@Data
@Schema(description = "登录请求DTO")
public class LoginRequestDTO {

    @Schema(description = "微信登录code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "微信登录code不能为空")
    private String code;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatarUrl;

    @Schema(description = "设备类型：1-微信小程序，2-Android，3-iOS，4-Web")
    private Integer deviceType;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备标识（UUID）")
    private String deviceId;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "用户代理")
    private String userAgent;
}
