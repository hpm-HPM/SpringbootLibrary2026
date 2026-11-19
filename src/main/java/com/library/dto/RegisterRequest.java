package com.library.dto;

import java.time.LocalDate;

public record RegisterRequest(
        String name,
        String password,
        String phone,
        String role,
        String major,
        Integer permissionLevel,
        LocalDate entryTime,
        String area,
        String duty,
        String grade,
        String college,
        String title
) {
}