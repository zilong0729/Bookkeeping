package com.bookkeeping.annotation;

import java.lang.annotation.*;

/**
 * 管理员权限注解
 * 标注在Controller方法上，表示该接口仅管理员可访问
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {
}
