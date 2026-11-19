package com.library.controller;

import com.library.common.ApiResponse;
import com.library.common.PageResult;
import com.library.common.RequirePermission;
import com.library.dto.UserDto;
import com.library.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequirePermission("用户信息查询")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageResult<UserDto>> search(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String major) {
        return ApiResponse.ok(userService.search(page, size, keyword, role, major));
    }

    @GetMapping("/majors")
    public ApiResponse<List<String>> majors() {
        return ApiResponse.ok(userService.majors());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> detail(@PathVariable String id) {
        return ApiResponse.ok(userService.detail(id));
    }
}