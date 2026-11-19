package com.library.dto;

import jakarta.validation.constraints.NotBlank;

public record ReservationRequest(@NotBlank(message = "图书 id 不能为空") String bookId) {
}