package org.example.umc11th.domain.book.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.book.dto.BookRequestDTO;
import org.example.umc11th.domain.book.dto.BookResponseDTO;
import org.example.umc11th.domain.book.service.BookService;
import org.example.umc11th.global.apiPayload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    public ApiResponse<List<BookResponseDTO>> getBooks(@RequestParam(required = false) String keyword) {
        return ApiResponse.onSuccess(bookService.getAllBooks(keyword));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookResponseDTO> createBook(@RequestBody
                                 @Valid BookRequestDTO request) {
        return ApiResponse.onSuccess(bookService.createBook(request));
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<BookResponseDTO>> getBooksByCategoryId(@PathVariable Long categoryId) {
        return ApiResponse.onSuccess(bookService.getBooksByCategoryId(categoryId));
    }
}
