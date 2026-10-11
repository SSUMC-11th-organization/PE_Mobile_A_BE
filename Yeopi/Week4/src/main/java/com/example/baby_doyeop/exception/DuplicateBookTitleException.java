package com.example.baby_doyeop.exception;

public class DuplicateBookTitleException extends RuntimeException {

    public DuplicateBookTitleException(String title) {
        super("이미 등록된 제목의 도서입니다. (title=" + title + ")");
    }
}
