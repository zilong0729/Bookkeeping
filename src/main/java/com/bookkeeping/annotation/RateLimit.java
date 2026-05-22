package com.bookkeeping.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 基于Redis滑动窗口实现分布式限流
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 窗口内允许的最大请求数
     */
    double permitsPerSecond() default 10.0;

    /**
     * 窗口大小（毫秒）
     * 默认1000ms（1秒）
     */
    long windowMs() default 1000;

    /**
     * 提示信息
     */
    String message() default "请求过于频繁，请稍后重试";
}
