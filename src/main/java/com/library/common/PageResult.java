package com.library.common;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResult<T>(List<T> content, long total, int page, int size, int totalPages) {

    public static <T> PageResult<T> of(Page<T> page, int pageNo, int size) {
        return new PageResult<>(page.getContent(), page.getTotalElements(), pageNo, size, page.getTotalPages());
    }

    public static <T> PageResult<T> empty(int pageNo, int size) {
        return new PageResult<>(List.of(), 0, pageNo, size, 0);
    }
}