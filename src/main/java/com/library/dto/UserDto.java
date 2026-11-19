package com.library.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDto(
        String id,
        String name,
        String phone,
        String role,
        String major,
        LocalDateTime loginTime,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        String[] permissions,
        Integer permissionLevel,
        LocalDate entryTime,
        String area,
        String duty,
        String grade,
        String college,
        String title
) {
}