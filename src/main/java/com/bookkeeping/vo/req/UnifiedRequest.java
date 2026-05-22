package com.bookkeeping.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一请求包装类")
public class UnifiedRequest<T> {

    @Schema(description = "请求数据")
    @NotNull(message = "请求数据不能为空")
    @Valid
    private T data;

    @Schema(description = "请求时间戳")
    private Long timestamp;

    @Schema(description = "请求签名")
    private String sign;
}
