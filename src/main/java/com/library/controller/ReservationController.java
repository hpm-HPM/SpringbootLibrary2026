package com.library.controller;

import com.library.common.ApiResponse;
import com.library.common.LoginUser;
import com.library.common.PageResult;
import com.library.common.RequirePermission;
import com.library.config.LoginInterceptor;
import com.library.dto.ReservationDto;
import com.library.dto.ReservationRequest;
import com.library.service.ReservationService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @RequirePermission("预约图书")
    public ApiResponse<ReservationDto> reserve(
            @RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user,
            @RequestBody ReservationRequest request) {
        return ApiResponse.ok(reservationService.reserve(user.id(), request.bookId()));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("预约图书")
    public ApiResponse<Void> cancel(@RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user,
                                    @PathVariable String id) {
        reservationService.cancel(user.id(), id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/my")
    public ApiResponse<PageResult<ReservationDto>> my(
            @RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(reservationService.my(user.id(), page, size, status));
    }

    @GetMapping("/search")
    @RequirePermission("预约管理")
    public ApiResponse<PageResult<ReservationDto>> search(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(reservationService.search(page, size, status, keyword));
    }
}