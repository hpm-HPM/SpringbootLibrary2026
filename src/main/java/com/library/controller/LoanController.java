package com.library.controller;

import com.library.common.ApiResponse;
import com.library.common.LoginUser;
import com.library.common.PageResult;
import com.library.common.RequirePermission;
import com.library.config.LoginInterceptor;
import com.library.dto.BorrowRequest;
import com.library.dto.LoanDto;
import com.library.dto.ReturnRequest;
import com.library.service.LoanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/borrow")
    @RequirePermission("借书")
    public ApiResponse<LoanDto> borrow(@RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user,
                                       @RequestBody BorrowRequest request) {
        return ApiResponse.ok(loanService.borrow(user.id(), request.bookId()));
    }

    @PostMapping("/return")
    @RequirePermission("还书")
    public ApiResponse<LoanDto> returnBook(@RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user,
                                           @RequestBody ReturnRequest request) {
        return ApiResponse.ok(loanService.returnBook(user.id(), request.copyId()));
    }

    @GetMapping("/my")
    public ApiResponse<PageResult<LoanDto>> my(
            @RequestAttribute(LoginInterceptor.REQUEST_USER) LoginUser user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        return ApiResponse.ok(loanService.my(user.id(), page, size, filter, start, end));
    }

    @GetMapping("/search")
    @RequirePermission("借阅记录查询")
    public ApiResponse<PageResult<LoanDto>> search(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "false") boolean overdue,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        return ApiResponse.ok(loanService.search(page, size, keyword, status, overdue, start, end));
    }
}