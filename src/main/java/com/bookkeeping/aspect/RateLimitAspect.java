package com.bookkeeping.aspect;

import com.bookkeeping.annotation.RateLimit;
import com.bookkeeping.exception.BusinessException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面
 * 基于Guava RateLimiter实现接口限流
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    /**
     * 使用Guava Cache存储RateLimiter，按方法+IP维度限流
     */
    private final LoadingCache<String, RateLimiter> limiters = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, RateLimiter>() {
                @Override
                public RateLimiter load(String key) {
                    // 默认每秒10个请求
                    return RateLimiter.create(10.0);
                }
            });

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        // 获取客户端IP
        String clientIp = getClientIp();

        // 构建限流key：类名.方法名:IP
        String key = className + "." + methodName + ":" + clientIp;

        // 获取或创建RateLimiter
        RateLimiter rateLimiter;
        try {
            rateLimiter = limiters.get(key);
            // 动态调整限流速率
            double permitsPerSecond = rateLimit.permitsPerSecond();
            if (rateLimiter.getRate() != permitsPerSecond) {
                rateLimiter.setRate(permitsPerSecond);
            }
        } catch (Exception e) {
            log.error("获取限流器失败", e);
            // 限流器异常时放行，避免影响正常业务
            return point.proceed();
        }

        // 尝试获取许可
        boolean acquired = rateLimiter.tryAcquire(rateLimit.timeout(), TimeUnit.MILLISECONDS);
        if (!acquired) {
            log.warn("接口限流触发: key={}, ip={}", key, clientIp);
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
