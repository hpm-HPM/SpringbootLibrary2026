package com.library.aop;

import com.library.config.LoginInterceptor;
import com.library.common.LoginUser;
import com.library.entity.OperationLog;
import com.library.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogService operationLogService;

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Around("execution(* com.library.controller..*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        long start = System.currentTimeMillis();
        OperationLog entry = new OperationLog();
        entry.setRequestUri(truncate(request.getRequestURI(), 200));
        entry.setHttpMethod(request.getMethod());
        entry.setModule(module(request.getRequestURI()));
        entry.setAction(joinPoint.getSignature().getName());
        entry.setParams(truncate(request.getQueryString(), 2000));
        entry.setIp(truncate(clientIp(request), 60));

        LoginUser user = currentUser(request);
        if (user != null) {
            entry.setUserId(user.id());
            entry.setUserName(user.name());
        }

        boolean success = true;
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            success = false;
            entry.setErrorMessage(truncate(throwable.getMessage(), 500));
            throw throwable;
        } finally {
            entry.setSuccess(success);
            entry.setDurationMs(System.currentTimeMillis() - start);
            try {
                operationLogService.save(entry);
            } catch (Exception exception) {
                log.warn("操作日志保存失败: {}", exception.getMessage());
            }
        }
    }

    private LoginUser currentUser(HttpServletRequest request) {
        Object value = request.getAttribute(LoginInterceptor.REQUEST_USER);
        if (value instanceof LoginUser loginUser) {
            return loginUser;
        }
        HttpSession session = request.getSession(false);
        Object sessionValue = session == null ? null : session.getAttribute(LoginInterceptor.SESSION_USER);
        return sessionValue instanceof LoginUser loginUser ? loginUser : null;
    }

    private String module(String uri) {
        String prefix = "/api/";
        if (uri != null && uri.startsWith(prefix)) {
            String rest = uri.substring(prefix.length());
            int index = rest.indexOf('/');
            return index < 0 ? rest : rest.substring(0, index);
        }
        return "api";
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}