package org.example.umc11th.domain.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.umc11th.domain.book.entity.Book;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByCategoryId(Long categoryId);
}
