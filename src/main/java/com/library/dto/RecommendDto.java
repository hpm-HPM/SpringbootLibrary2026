package com.library.dto;

import java.time.LocalDateTime;

public record RecommendDto(
        String id,
        String userId,
        String userName,
        String bookName,
        String author,
        String publisher,
        Double price,
        String status,
        String reason,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}