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
@Schema(description = "登录请求VO")
public class LoginReqVO {

    @Schema(description = "微信授权码", required = true)
    @NotBlank(message = "微信授权码不能为空")
    private String code;

    @Schema(description = "设备类型：1-小程序，2-Android，3-iOS")
    private Integer deviceType;

    @Schema(description = "设备ID")
    private String deviceId;
}
