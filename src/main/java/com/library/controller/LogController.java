package com.library.controller;

import com.library.common.ApiResponse;
import com.library.common.PageResult;
import com.library.common.RequirePermission;
import com.library.entity.OperationLog;
import com.library.service.OperationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@RequirePermission("操作日志查询")
public class LogController {

    private final OperationLogService operationLogService;

    public LogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public ApiResponse<PageResult<OperationLog>> search(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(operationLogService.search(page, size, module, keyword));
    }
}