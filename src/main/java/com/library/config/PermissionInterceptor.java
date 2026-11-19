package com.library.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.ApiResponse;
import com.library.common.LoginUser;
import com.library.common.RequirePermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public PermissionInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !(handler instanceof HandlerMethod method)) {
            return true;
        }
        RequirePermission annotation = AnnotatedElementUtils.findMergedAnnotation(method.getMethod(),
                RequirePermission.class);
        if (annotation == null) {
            annotation = AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(), RequirePermission.class);
        }
        if (annotation == null) {
            return true;
        }
        Object value = request.getAttribute(LoginInterceptor.REQUEST_USER);
        if (!(value instanceof LoginUser loginUser)) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "登录已失效，请重新登录");
            return false;
        }
        if (!loginUser.hasPermission(annotation.value())) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, "没有权限执行此操作");
            return false;
        }
        return true;
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(message));
    }
}