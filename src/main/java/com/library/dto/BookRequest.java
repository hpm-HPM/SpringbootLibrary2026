package com.library.dto;

public record BookRequest(
        String title,
        String author,
        String type,
        String publisher,
        Integer days,
        Double price,
        Integer stock,
        String introduction,
        String tags
) {
}