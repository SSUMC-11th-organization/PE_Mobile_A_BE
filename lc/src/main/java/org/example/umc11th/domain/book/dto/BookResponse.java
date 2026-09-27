package org.example.umc11th.domain.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.umc11th.domain.book.entity.Book;

public record BookResponse(
        Long bookId,
        Long categoryId,
        String title,
        String description,
        Boolean isAvailable
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getCategory().getId(),
                book.getTitle(),
                book.getDescription(),
                book.getIsAvailable()
        );
    }

    public record BookCreateRequest(
            @NotNull Long categoryId,
            @NotBlank String title,
            String description
    ) {
    }
}


