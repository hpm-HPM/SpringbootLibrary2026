package com.library.dto;

public record RecommendRequest(
        String bookName,
        String author,
        String publisher,
        Double price
) {
}