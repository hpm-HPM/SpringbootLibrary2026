package com.library.dto;

import jakarta.validation.constraints.NotBlank;

public record ReturnRequest(@NotBlank(message = "副本 id 不能为空") String copyId) {
}