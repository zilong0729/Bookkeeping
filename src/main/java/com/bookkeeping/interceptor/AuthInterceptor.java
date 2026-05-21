package com.bookkeeping.interceptor;

import com.bookkeeping.common.Result;
import com.bookkeeping.utils.JwtUtil;
import com.bookkeeping.utils.RedisUtil;
import com.bookkeeping.utils.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token
        String authHeader = request.getHeader(HEADER_AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            writeErrorResponse(response, Result.unauthorized("请先登录"));
            return false;
        }

        String token = authHeader.substring(TOKEN_PREFIX.length());

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            writeErrorResponse(response, Result.unauthorized("登录已过期，请重新登录"));
            return false;
        }

        // 获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String openid = jwtUtil.getOpenidFromToken(token);

        if (userId == null || openid == null) {
            writeErrorResponse(response, Result.unauthorized("登录信息无效，请重新登录"));
            return false;
        }

        // 验证token是否在Redis中存在（支持后端强制下线）
        String cachedToken = redisUtil.getUserToken(userId);
        if (cachedToken == null || !cachedToken.equals(token)) {
            writeErrorResponse(response, Result.unauthorized("登录已失效，请重新登录"));
            return false;
        }

        // 将用户信息存入上下文
        UserContext.setUserId(userId);
        UserContext.setOpenid(openid);
        UserContext.setRole(jwtUtil.getRoleFromToken(token));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除用户上下文
        UserContext.clear();
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, Result<Void> result) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
        writer.close();
    }
}
