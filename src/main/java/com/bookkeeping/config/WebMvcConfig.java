package com.bookkeeping.config;

import com.bookkeeping.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 公开接口
                        "/user/login",
                        "/user/register",
                        "/admin/login",
                        // 监控接口（无需认证）
                        "/monitor/**",
                        // Swagger/API文档
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        // Druid监控
                        "/druid/**",
                        // 静态资源
                        "/favicon.ico",
                        "/error"
                );
    }
}
