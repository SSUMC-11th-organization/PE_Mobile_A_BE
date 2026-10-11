package com.example.baby_doyeop.dto;

import com.example.baby_doyeop.entity.Book;

// Entity를 그대로 내보내지 않고 API에 필요한 값만 골라 응답합니다.
public record BookResponse(
        Long bookId,
        String title,
        String description,
        String categoryName,
        Boolean isAvailable
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getBookId(),
                book.getTitle(),
                book.getDescription(),
                book.getCategory().getName(),
                book.getIsAvailable()
        );
    }
}
