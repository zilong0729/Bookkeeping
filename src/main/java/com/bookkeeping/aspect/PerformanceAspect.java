package com.bookkeeping.aspect;

import com.bookkeeping.utils.PerformanceMonitor;
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

/**
 * 性能监控切面
 * 记录Controller接口的执行耗时
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PerformanceAspect {

    private final PerformanceMonitor performanceMonitor;

    /**
     * 拦截所有Controller方法
     */
    @Around("execution(* com.bookkeeping.controller.*.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取方法信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String key = className + "." + methodName;

        try {
            return point.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录性能指标
            performanceMonitor.record(key, executionTime);

            // 慢查询警告（超过1秒）
            if (executionTime > 1000) {
                String requestUri = getRequestUri();
                log.warn("慢接口警告: {} {} 耗时{}ms", key, requestUri, executionTime);
            }
        }
    }

    private String getRequestUri() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getRequestURI();
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }
}
