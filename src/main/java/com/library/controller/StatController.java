package com.library.controller;

import com.library.common.ApiResponse;
import com.library.common.RequirePermission;
import com.library.dto.StatsItem;
import com.library.service.StatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequirePermission("借阅数据统计")
public class StatController {

    private final StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    @GetMapping("/types")
    public ApiResponse<List<StatsItem>> byType() {
        return ApiResponse.ok(statService.byType());
    }

    @GetMapping("/majors")
    public ApiResponse<List<StatsItem>> byMajor() {
        return ApiResponse.ok(statService.byMajor());
    }
}