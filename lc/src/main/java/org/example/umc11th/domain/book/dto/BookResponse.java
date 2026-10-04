package org.example.umc11th.domain.book.dto;

import org.example.umc11th.domain.book.entity.Book;

public record BookResponse(
        Long bookId,
        String categoryName,
        String title,
        String description,
        Boolean isAvailable
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getCategory().getName(),
                book.getTitle(),
                book.getDescription(),
                book.getIsAvailable()
        );
    }
}


