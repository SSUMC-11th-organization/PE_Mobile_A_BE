package org.example.umc11th.domain.book.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.book.dto.BookResponse;
import org.example.umc11th.domain.book.service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    public List<BookResponse> getBooks() {
        return bookService.getAllBooks();
    }

    @PostMapping
    public String createBook(@RequestBody
                                 @Valid BookResponse.BookCreateRequest request) {
        bookService.createBook(request);
        return "Book created";
    }

    @GetMapping("/category/{categoryId}")
    public List<BookResponse> getBooksByCategoryId(@PathVariable Long categoryId) {
        return  bookService.getBooksByCategoryId(categoryId);
    }
}
