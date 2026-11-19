package com.library.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.ApiResponse;
import com.library.common.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER = "loginUser";
    public static final String REQUEST_USER = "currentUser";

    private final ObjectMapper objectMapper;

    public LoginInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute(SESSION_USER);
        if (!(value instanceof LoginUser loginUser)) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "登录已失效，请重新登录");
            return false;
        }
        request.setAttribute(REQUEST_USER, loginUser);
        return true;
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(message));
    }
}