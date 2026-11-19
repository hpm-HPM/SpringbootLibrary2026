package com.library.dto;

import java.time.LocalDateTime;

public record AttachmentDto(
        String id,
        String bookId,
        String fileName,
        String originalName,
        String contentType,
        Long fileSize,
        String url,
        Boolean isCover,
        Integer sortOrder,
        LocalDateTime createTime
) {
}