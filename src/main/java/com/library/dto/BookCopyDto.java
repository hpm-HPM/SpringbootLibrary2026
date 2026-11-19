package com.library.dto;

import java.time.LocalDateTime;

public record BookCopyDto(
        String id,
        String bookId,
        Boolean isBorrowed,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}