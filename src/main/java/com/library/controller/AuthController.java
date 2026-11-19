package com.library.controller;

import com.library.common.ApiResponse;
import com.library.config.LoginInterceptor;
import com.library.common.LoginUser;
import com.library.dto.LoginRequest;
import com.library.dto.RegisterRequest;
import com.library.dto.UserDto;
import com.library.service.AuthService;
import com.library.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<UserDto> login(@Valid @RequestBody LoginRequest request,
                                      HttpServletRequest httpRequest) {
        UserDto user = authService.login(request);
        HttpSession session = httpRequest.getSession(true);
        httpRequest.changeSessionId();
        session.setAttribute(LoginInterceptor.SESSION_USER, LoginUser.from(user));
        return ApiResponse.ok(user);
    }

    @PostMapping("/register")
    public ApiResponse<UserDto> register(@RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute(LoginInterceptor.SESSION_USER);
        if (!(value instanceof LoginUser loginUser)) {
            return ApiResponse.ok(null);
        }
        try {
            return ApiResponse.ok(userService.detail(loginUser.id()));
        } catch (Exception exception) {
            session.invalidate();
            return ApiResponse.ok(null);
        }
    }
}