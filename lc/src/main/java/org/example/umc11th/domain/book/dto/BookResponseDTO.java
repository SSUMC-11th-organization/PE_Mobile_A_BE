package org.example.umc11th.domain.book.dto;

import org.example.umc11th.domain.book.entity.Book;

public record BookResponseDTO(
        Long bookId,
        String categoryName,
        String title,
        String description,
        Boolean isAvailable
) {
    public static BookResponseDTO from(Book book) {
        return new BookResponseDTO(
                book.getId(),
                book.getCategory().getName(),
                book.getTitle(),
                book.getDescription(),
                book.getIsAvailable()
        );
    }
}


