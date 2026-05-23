package com.bookkeeping.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 支持多维度限流：全局、用户、IP
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.DEFAULT;

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

    enum LimitType {
        /**
         * 默认全局限流
         */
        DEFAULT,
        /**
         * 基于用户ID限流
         */
        USER,
        /**
         * 基于IP地址限流
         */
        IP,
        /**
         * 基于用户+接口限流
         */
        USER_INTERFACE
    }
}
