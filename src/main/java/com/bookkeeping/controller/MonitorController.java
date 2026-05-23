package com.bookkeeping.controller;

import com.bookkeeping.monitor.HealthCheck;
import com.bookkeeping.vo.resp.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统监控接口
 */
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
@Tag(name = "系统监控", description = "系统健康检查和监控接口")
public class MonitorController {

    private final HealthCheck healthCheck;

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查系统各组件健康状态")
    public Result<Map<String, Object>> health() {
        Map<String, Object> health = healthCheck.check();
        return Result.success(health);
    }
}
