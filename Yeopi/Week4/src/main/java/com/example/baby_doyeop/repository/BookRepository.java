package com.example.baby_doyeop.repository;

import com.example.baby_doyeop.entity.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // category를 함께 가져와 응답 DTO에서 categoryName을 읽을 때 N+1 쿼리가 생기지 않게 합니다.
    @EntityGraph(attributePaths = "category")
    List<Book> findAllByOrderByBookIdDesc();

    @EntityGraph(attributePaths = "category")
    List<Book> findByCategory_CategoryIdOrderByBookIdDesc(Long categoryId);

    // [선택 2] GET /books?keyword=... 제목 검색 (LIKE %keyword%)
    @EntityGraph(attributePaths = "category")
    List<Book> findByTitleContainingOrderByBookIdDesc(String keyword);

    boolean existsByTitle(String title);
}
