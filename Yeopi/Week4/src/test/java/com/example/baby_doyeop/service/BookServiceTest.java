package com.example.baby_doyeop.service;

import com.example.baby_doyeop.dto.BookResponse;
import com.example.baby_doyeop.dto.CreateBookRequest;
import com.example.baby_doyeop.entity.Book;
import com.example.baby_doyeop.entity.Category;
import com.example.baby_doyeop.exception.CategoryNotFoundException;
import com.example.baby_doyeop.exception.DuplicateBookTitleException;
import com.example.baby_doyeop.repository.BookRepository;
import com.example.baby_doyeop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(BookService.class)
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category literature;

    @BeforeEach
    void setUp() {
        // 워크북 02_seed.sql과 같은 데이터
        literature = categoryRepository.save(new Category("문학"));
        Category science = categoryRepository.save(new Category("과학"));
        bookRepository.save(new Book(literature, "달빛 도서관", "소설"));
        bookRepository.save(new Book(literature, "겨울의 편지", "에세이"));
        bookRepository.save(new Book(science, "우주를 읽는 법", "과학 교양"));
    }

    @Test
    void 전체_도서를_최신_등록순으로_카테고리_이름과_함께_반환한다() {
        List<BookResponse> books = bookService.getBooks(null);

        assertThat(books).extracting(BookResponse::title)
                .containsExactly("우주를 읽는 법", "겨울의 편지", "달빛 도서관");
        assertThat(books.get(0).categoryName()).isEqualTo("과학");
        assertThat(books.get(0).isAvailable()).isTrue();
    }

    @Test
    void keyword가_있으면_제목에_포함된_도서만_반환한다() {
        assertThat(bookService.getBooks("도서관")).extracting(BookResponse::title)
                .containsExactly("달빛 도서관");
        assertThat(bookService.getBooks("없는 제목")).isEmpty();
        assertThat(bookService.getBooks("  ")).hasSize(3);
    }

    @Test
    void 카테고리별_도서를_조회하고_없는_카테고리는_예외를_던진다() {
        assertThat(bookService.getBooksByCategory(literature.getCategoryId()))
                .extracting(BookResponse::title)
                .containsExactly("겨울의 편지", "달빛 도서관");

        assertThatThrownBy(() -> bookService.getBooksByCategory(999L))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void 새_도서를_저장하고_응답_DTO로_반환한다() {
        BookResponse created = bookService.createBook(
                new CreateBookRequest(literature.getCategoryId(), "  봄의 정원  ", "시집"));

        assertThat(created.bookId()).isNotNull();
        assertThat(created.title()).isEqualTo("봄의 정원");
        assertThat(created.categoryName()).isEqualTo("문학");
        assertThat(created.isAvailable()).isTrue();
        assertThat(bookRepository.count()).isEqualTo(4);
    }

    @Test
    void 존재하지_않는_카테고리면_저장하지_않고_예외를_던진다() {
        assertThatThrownBy(() -> bookService.createBook(new CreateBookRequest(999L, "새 책", null)))
                .isInstanceOf(CategoryNotFoundException.class);
        assertThat(bookRepository.count()).isEqualTo(3);
    }

    @Test
    void 같은_제목이_이미_있으면_예외를_던진다() {
        assertThatThrownBy(() -> bookService.createBook(
                new CreateBookRequest(literature.getCategoryId(), "달빛 도서관", null)))
                .isInstanceOf(DuplicateBookTitleException.class);
    }
}
