package com.example.baby_doyeop.service;

import com.example.baby_doyeop.dto.BookResponse;
import com.example.baby_doyeop.dto.CreateBookRequest;
import com.example.baby_doyeop.entity.Book;
import com.example.baby_doyeop.entity.Category;
import com.example.baby_doyeop.exception.CategoryNotFoundException;
import com.example.baby_doyeop.exception.DuplicateBookTitleException;
import com.example.baby_doyeop.repository.BookRepository;
import com.example.baby_doyeop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    // keyword가 없으면 전체 목록, 있으면 제목 검색 결과를 최신 등록순으로 반환합니다.
    public List<BookResponse> getBooks(String keyword) {
        List<Book> books = StringUtils.hasText(keyword)
                ? bookRepository.findByTitleContainingOrderByBookIdDesc(keyword.strip())
                : bookRepository.findAllByOrderByBookIdDesc();
        return books.stream().map(BookResponse::from).toList();
    }

    public List<BookResponse> getBooksByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException(categoryId);
        }
        return bookRepository.findByCategory_CategoryIdOrderByBookIdDesc(categoryId).stream()
                .map(BookResponse::from)
                .toList();
    }

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        String title = request.title().strip();
        if (bookRepository.existsByTitle(title)) {
            throw new DuplicateBookTitleException(title);
        }

        Book book = new Book(category, title, request.description());
        return BookResponse.from(bookRepository.save(book));
    }
}
