package org.example.umc11th.domain.book.service;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.book.dto.BookRequest;
import org.example.umc11th.domain.book.dto.BookResponse;
import org.example.umc11th.domain.book.entity.Book;
import org.example.umc11th.domain.book.exception.BookErrorCode;
import org.example.umc11th.domain.book.exception.BookException;
import org.example.umc11th.domain.book.repository.BookRepository;
import org.example.umc11th.domain.category.entity.Category;
import org.example.umc11th.domain.category.repository.CategoryRepository;
import org.example.umc11th.domain.category.exception.CategoryException;
import org.example.umc11th.domain.category.exception.CategoryErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public List<BookResponse> getAllBooks(String keyword) {
        List<Book> books = (keyword == null || keyword.isBlank())
                ? bookRepository.findAllByOrderByIdDesc()
                : bookRepository.findByTitleContainingOrderByIdDesc(keyword);

        return books.stream().map(BookResponse::from).toList();
    }

    @Transactional
    public BookResponse createBook(BookRequest request){
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));

        if(bookRepository.existsByTitle(request.title())){
            throw new BookException(BookErrorCode.BOOK_TITLE_DUPLICATED);
        }

        Book book = Book.builder()
                .category(category)
                .title(request.title())
                .description(request.description())
                .build();
        return BookResponse.from(bookRepository.save(book));
    }

    public List<BookResponse> getBooksByCategoryId(Long categoryId){
        return bookRepository.findByCategoryId(categoryId).stream()
                .map(BookResponse::from)
                .toList();
    }
}
