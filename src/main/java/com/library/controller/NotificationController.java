package com.library.controller;

import com.library.common.ApiResponse;
import com.library.common.LoginUser;
import com.library.common.PageResult;
import com.library.config.LoginInterceptor;
import com.library.dto.NotificationDto;
import com.library.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<PageResult<NotificationDto>> list(
            @RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(notificationService.list(user.id(), page, size));
    }

    @GetMapping("/unread")
    public ApiResponse<List<NotificationDto>> unread(
            @RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user) {
        return ApiResponse.ok(notificationService.unread(user.id()));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(
            @RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user) {
        return ApiResponse.ok(notificationService.unreadCount(user.id()));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user,
                                      @PathVariable String id) {
        notificationService.markRead(user.id(), id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead(@RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user) {
        notificationService.markAllRead(user.id());
        return ApiResponse.ok(null);
    }
}