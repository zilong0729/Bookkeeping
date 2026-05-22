package com.bookkeeping.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * XSS过滤器
 * 对所有请求进行XSS防护
 */
@Slf4j
@Component
@WebFilter(urlPatterns = "/*")
@Order(1)
public class XssFilter implements Filter {

    private static final String[] EXCLUDE_PATHS = {
            "/doc.html",
            "/webjars/",
            "/v3/api-docs",
            "/swagger-ui",
            "/favicon.ico"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        // 跳过静态资源和接口文档
        if (isExcludePath(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // 包装请求，进行XSS过滤
        XssHttpServletRequestWrapper wrappedRequest = new XssHttpServletRequestWrapper(httpRequest);
        chain.doFilter(wrappedRequest, response);
    }

    private boolean isExcludePath(String requestURI) {
        for (String excludePath : EXCLUDE_PATHS) {
            if (requestURI.contains(excludePath)) {
                return true;
            }
        }
        return false;
    }
}
