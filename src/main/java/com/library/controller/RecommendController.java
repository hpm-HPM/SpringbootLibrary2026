package com.library.controller;

import com.library.common.ApiResponse;
import com.library.common.LoginUser;
import com.library.common.RequirePermission;
import com.library.config.LoginInterceptor;
import com.library.dto.RecommendDto;
import com.library.dto.RecommendRequest;
import com.library.dto.ReviewRequest;
import com.library.service.RecommendService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @PostMapping("/check")
    @RequirePermission("推荐图书")
    public ApiResponse<Map<String, Object>> check(
            @RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user,
            @RequestBody RecommendRequest request) {
        return ApiResponse.ok(recommendService.check(user.id(), request));
    }

    @GetMapping("/mine")
    @RequirePermission("推荐图书")
    public ApiResponse<List<RecommendDto>> mine(
            @RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user) {
        return ApiResponse.ok(recommendService.mine(user.id()));
    }

    @GetMapping("/pending")
    @RequirePermission("审阅推荐图书")
    public ApiResponse<List<RecommendDto>> pending() {
        return ApiResponse.ok(recommendService.pending());
    }

    @PostMapping("/review")
    @RequirePermission("审阅推荐图书")
    public ApiResponse<RecommendDto> review(@RequestBody ReviewRequest request) {
        return ApiResponse.ok(recommendService.review(request));
    }
}