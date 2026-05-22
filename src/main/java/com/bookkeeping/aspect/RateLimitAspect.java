package com.bookkeeping.aspect;

import com.bookkeeping.annotation.RateLimit;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 限流切面
 * 基于Redis滑动窗口实现分布式接口限流
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisUtil redisUtil;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        // 获取客户端IP
        String clientIp = getClientIp();

        // 构建限流key：rate_limit:类名.方法名:IP
        String key = "rate_limit:" + className + "." + methodName + ":" + clientIp;

        // 计算窗口大小和限制次数
        long windowMs = rateLimit.windowMs();
        int limit = (int) Math.ceil(rateLimit.permitsPerSecond());

        // 尝试获取许可
        boolean acquired = redisUtil.tryAcquire(key, windowMs, limit);
        if (!acquired) {
            log.warn("接口限流触发: key={}, ip={}, limit={}/{}ms", key, clientIp, limit, windowMs);
            throw new BusinessException(429, rateLimit.message());
        }

        return point.proceed();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "unknown";
            }
            HttpServletRequest request = attributes.getRequest();

            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            // 多次代理时取第一个IP
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }
}
