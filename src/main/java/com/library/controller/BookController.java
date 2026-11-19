package com.library.controller;

import com.library.common.ApiResponse;
import com.library.common.PageResult;
import com.library.common.RequirePermission;
import com.library.dto.AttachmentDto;
import com.library.dto.BookCopyDto;
import com.library.dto.BookDetailDto;
import com.library.dto.BookDto;
import com.library.dto.BookRequest;
import com.library.service.BookService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ApiResponse<PageResult<BookDto>> search(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String publisher) {
        return ApiResponse.ok(bookService.search(page, size, keyword, type, status, author, publisher));
    }

    @GetMapping("/borrowable")
    public ApiResponse<List<BookDto>> borrowable(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(bookService.borrowable(keyword));
    }

    @GetMapping("/types")
    public ApiResponse<List<String>> types() {
        return ApiResponse.ok(bookService.types());
    }

    @GetMapping("/{id}")
    public ApiResponse<BookDetailDto> detail(@PathVariable String id) {
        return ApiResponse.ok(bookService.detail(id));
    }

    @PostMapping
    @RequirePermission("添加书籍")
    public ApiResponse<BookDto> add(@RequestBody BookRequest request) {
        return ApiResponse.ok(bookService.add(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("编辑图书")
    public ApiResponse<BookDto> update(@PathVariable String id, @RequestBody BookRequest request) {
        return ApiResponse.ok(bookService.update(id, request));
    }

    @PostMapping("/{id}/discard")
    @RequirePermission("废弃图书")
    public ApiResponse<Void> discard(@PathVariable String id) {
        bookService.discard(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{bookId}/copies")
    @RequirePermission("图书副本编辑")
    public ApiResponse<List<BookCopyDto>> copies(@PathVariable String bookId) {
        return ApiResponse.ok(bookService.copies(bookId));
    }

    @PostMapping("/{bookId}/copies")
    @RequirePermission("图书副本编辑")
    public ApiResponse<BookCopyDto> addCopy(@PathVariable String bookId) {
        return ApiResponse.ok(bookService.addCopy(bookId));
    }

    @DeleteMapping("/copies/{copyId}")
    @RequirePermission("图书副本编辑")
    public ApiResponse<Void> deleteCopy(@PathVariable String copyId) {
        bookService.deleteCopy(copyId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{bookId}/attachments")
    @RequirePermission("编辑图书")
    public ApiResponse<List<AttachmentDto>> upload(
            @PathVariable String bookId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(defaultValue = "false") boolean cover) {
        return ApiResponse.ok(bookService.upload(bookId, files, cover));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @RequirePermission("编辑图书")
    public ApiResponse<Void> deleteAttachment(@PathVariable String attachmentId) {
        bookService.deleteAttachment(attachmentId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/attachments/{attachmentId}/cover")
    @RequirePermission("编辑图书")
    public ApiResponse<Void> setCover(@PathVariable String attachmentId) {
        bookService.setCover(attachmentId);
        return ApiResponse.ok(null);
    }
}