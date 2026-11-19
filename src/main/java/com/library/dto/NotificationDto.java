package com.library.dto;

import java.time.LocalDateTime;

public record NotificationDto(
        String id,
        String title,
        String content,
        String type,
        Boolean isRead,
        String link,
        LocalDateTime createTime
) {
}