package com.library.dto;

import java.time.LocalDateTime;

public record ReservationDto(
        String id,
        String bookId,
        String bookTitle,
        String coverUrl,
        String userId,
        String userName,
        String status,
        LocalDateTime notifyTime,
        LocalDateTime expireTime,
        LocalDateTime createTime
) {
}