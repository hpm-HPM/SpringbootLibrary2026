package com.library.dto;

import java.util.List;

public record BookDetailDto(
        BookDto book,
        List<BookCopyDto> copies,
        List<AttachmentDto> attachments
) {
}