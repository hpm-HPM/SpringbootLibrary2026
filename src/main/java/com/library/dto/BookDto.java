package com.library.dto;

import java.time.LocalDateTime;

public record BookDto(
        String id,
        String title,
        String author,
        String type,
        String publisher,
        Integer days,
        Double price,
        Integer stock,
        String status,
        String introduction,
        String tags,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        String coverUrl,
        long attachmentCount,
        long copyCount,
        long borrowedCount
) {
}