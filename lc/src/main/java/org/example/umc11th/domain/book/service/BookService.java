package org.example.umc11th.domain.book.service;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.book.dto.BookResponse;
import org.example.umc11th.domain.book.entity.Book;
import org.example.umc11th.domain.book.repository.BookRepository;
import org.example.umc11th.domain.category.entity.Category;
import org.example.umc11th.domain.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(BookResponse::from)
                .toList();
    }

    @Transactional
    public void createBook(BookResponse.BookCreateRequest request){
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Book book = Book.builder()
                .category(category)
                .title(request.title())
                .description(request.description())
                .build();
        bookRepository.save(book);
    }

    public List<BookResponse> getBooksByCategoryId(Long categoryId){
        return bookRepository.findByCategoryId(categoryId).stream()
                .map(BookResponse::from)
                .toList();
    }
}
