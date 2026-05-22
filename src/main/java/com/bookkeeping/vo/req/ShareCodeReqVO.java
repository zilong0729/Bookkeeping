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
@Schema(description = "分享码请求VO")
public class ShareCodeReqVO {

    @Schema(description = "分享码", required = true)
    @NotBlank(message = "分享码不能为空")
    private String shareCode;
}
