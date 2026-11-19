package com.library.dto;

import java.time.LocalDateTime;

public record LoanDto(
        String id,
        String bookCopyId,
        String bookId,
        String bookTitle,
        String bookAuthor,
        String bookType,
        Integer bookDays,
        String userId,
        String userName,
        String userRole,
        String userMajor,
        LocalDateTime borrowDate,
        LocalDateTime returnDate,
        LocalDateTime dueDate,
        String status,
        boolean overdue,
        LocalDateTime createTime
) {
}