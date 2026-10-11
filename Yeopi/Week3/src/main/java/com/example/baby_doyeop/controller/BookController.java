package com.example.baby_doyeop.controller;

import com.example.baby_doyeop.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // GET http://localhost:8080/books
    @GetMapping
    public List<Map<String, Object>> getBooks() {
        return bookService.getAllBooks();
    }

    // GET http://localhost:8080/books/category/{categoryId}
    @GetMapping("/category/{categoryId}")
    public List<Map<String, Object>> getBooksByCategory(@PathVariable Long categoryId) {
        return bookService.getBooksByCategory(categoryId);
    }

    // POST http://localhost:8080/books
    @PostMapping
    public String createBook(@RequestBody Map<String, Object> body) {
        bookService.createBook(body);
        return "도서 등록이 완료되었습니다!";
    }
}
