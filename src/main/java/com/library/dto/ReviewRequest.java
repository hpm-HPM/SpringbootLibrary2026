package com.library.dto;

public record ReviewRequest(
        String id,
        Boolean approve,
        String type,
        Integer days,
        Integer stock,
        String reason
) {
}