package com.bookkeeping.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 基于Guava RateLimiter实现
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 每秒允许的请求数
     */
    double permitsPerSecond() default 10.0;

    /**
     * 获取许可等待超时时间（毫秒）
     * 默认100ms，超过则限流
     */
    long timeout() default 100;

    /**
     * 提示信息
     */
    String message() default "请求过于频繁，请稍后重试";
}
