package com.umc.study.controller;

import com.umc.study.dto.BookResponse;
import com.umc.study.dto.CreateBookRequest;
import com.umc.study.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public List<BookResponse> getBooks(@RequestParam(name = "keyword", required = false) String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return bookService.getBooks();
        }
        return bookService.searchBooks(keyword.strip());
    }

    @GetMapping("/category/{categoryId}")
    public List<BookResponse> getBooksByCategory(@PathVariable("categoryId") Long categoryId) {
        return bookService.getBooksByCategory(categoryId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@Valid @RequestBody CreateBookRequest request) {
        return bookService.createBook(request);
    }
}
