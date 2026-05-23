package com.bookkeeping.aspect;

import com.bookkeeping.annotation.RateLimit;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.utils.RedisUtil;
import com.bookkeeping.utils.UserContext;
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
 * 限流切面 - 增强版
 * 支持多维度限流：全局、用户、IP、用户+接口
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisUtil redisUtil;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        String clientIp = getClientIp();
        Long userId = UserContext.getUserId();

        String key = buildLimitKey(rateLimit.limitType(), className, methodName, clientIp, userId);

        long windowMs = rateLimit.windowMs();
        int limit = (int) Math.ceil(rateLimit.permitsPerSecond());

        boolean acquired = redisUtil.tryAcquire(key, windowMs, limit);
        if (!acquired) {
            log.warn("限流触发: type={}, key={}, ip={}, userId={}, limit={}/{}ms",
                    rateLimit.limitType(), key, clientIp, userId, limit, windowMs);
            throw new BusinessException(429, rateLimit.message());
        }

        return point.proceed();
    }

    private String buildLimitKey(RateLimit.LimitType limitType, String className,
                                String methodName, String clientIp, Long userId) {
        String baseKey = "rate_limit:" + className + "." + methodName;

        switch (limitType) {
            case USER:
                return baseKey + ":user:" + (userId != null ? userId : "anonymous");
            case IP:
                return baseKey + ":ip:" + clientIp;
            case USER_INTERFACE:
                return baseKey + ":user:" + (userId != null ? userId : "anonymous") + ":ip:" + clientIp;
            case DEFAULT:
            default:
                return baseKey + ":ip:" + clientIp;
        }
    }

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
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }
}
