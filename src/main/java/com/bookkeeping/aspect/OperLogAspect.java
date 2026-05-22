package com.bookkeeping.aspect;

import com.bookkeeping.annotation.OperLog;
import com.bookkeeping.annotation.RequireAdmin;
import com.bookkeeping.entity.OperationLog;
import com.bookkeeping.exception.BusinessException;
import com.bookkeeping.service.AsyncLogService;
import com.bookkeeping.utils.SensitiveDataUtil;
import com.bookkeeping.utils.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 操作日志AOP切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final AsyncLogService asyncLogService;
    private final ObjectMapper objectMapper;

    // 敏感字段正则
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("password|pwd|secret|token", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("phone|mobile|tel", Pattern.CASE_INSENSITIVE);

    /**
     * 切入点：所有标注了 @OperLog 的方法
     */
    @Pointcut("@annotation(com.bookkeeping.annotation.OperLog)")
    public void operLogPointcut() {
    }

    /**
     * 管理员权限校验切入点
     */
    @Pointcut("@annotation(com.bookkeeping.annotation.RequireAdmin)")
    public void requireAdminPointcut() {
    }

    /**
     * 管理员权限校验
     */
    @Around("requireAdminPointcut()")
    public Object checkAdmin(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "无管理员权限");
        }
        return joinPoint.proceed();
    }

    /**
     * 操作日志记录
     */
    @Around("operLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperLog operLogAnnotation = signature.getMethod().getAnnotation(OperLog.class);

        OperationLog operationLog = new OperationLog();
        operationLog.setOperation(operLogAnnotation.value());
        operationLog.setMethod(joinPoint.getTarget().getClass().getName() + "." + signature.getName());
        operationLog.setCreateTime(LocalDateTime.now());

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setRequestMethod(request.getMethod());
            operationLog.setIp(getIpAddr(request));
        }

        // 获取当前用户信息
        Long userId = UserContext.getUserId();
        operationLog.setUserId(userId);

        // 记录请求参数
        if (operLogAnnotation.recordParams()) {
            try {
                Object[] args = joinPoint.getArgs();
                Map<String, Object> paramMap = new HashMap<>();
                String[] paramNames = signature.getParameterNames();
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof HttpServletRequest || args[i] instanceof HttpServletResponse
                            || args[i] instanceof MultipartFile) {
                        continue;
                    }
                    if (paramNames != null && paramNames[i] != null) {
                        Object value = maskSensitiveData(paramNames[i], args[i]);
                        paramMap.put(paramNames[i], value);
                    }
                }
                String params = objectMapper.writeValueAsString(paramMap);
                // 截断过长的参数
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
                operationLog.setRequestParams(params);
            } catch (Exception e) {
                log.warn("记录请求参数失败", e);
            }
        }

        Object result;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            operationLog.setStatus(1);

            // 记录返回结果
            if (operLogAnnotation.recordResult() && result != null) {
                String resultStr = objectMapper.writeValueAsString(result);
                if (resultStr.length() > 2000) {
                    resultStr = resultStr.substring(0, 2000) + "...";
                }
                operationLog.setResponseResult(resultStr);
            }
        } catch (Exception e) {
            operationLog.setStatus(0);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 1000) {
                errorMsg = errorMsg.substring(0, 1000) + "...";
            }
            operationLog.setErrorMsg(errorMsg);
            throw e;
        } finally {
            operationLog.setExecutionTime(System.currentTimeMillis() - startTime);
            // 异步保存日志（不阻塞主流程）
            asyncLogService.saveLog(operationLog);
        }

        return result;
    }

    /**
     * 获取客户端真实IP
     */
    private String getIpAddr(HttpServletRequest request) {
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
    }

    /**
     * 对敏感数据进行脱敏
     */
    private Object maskSensitiveData(String paramName, Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)) {
            return value;
        }
        String strValue = (String) value;
        
        // 密码类字段脱敏
        if (PASSWORD_PATTERN.matcher(paramName).find()) {
            return SensitiveDataUtil.maskPassword(strValue);
        }
        
        // 手机类字段脱敏
        if (PHONE_PATTERN.matcher(paramName).find()) {
            return SensitiveDataUtil.maskPhone(strValue);
        }
        
        // Token类脱敏
        if (paramName.toLowerCase().contains("token")) {
            return SensitiveDataUtil.maskToken(strValue);
        }
        
        return value;
    }
}
