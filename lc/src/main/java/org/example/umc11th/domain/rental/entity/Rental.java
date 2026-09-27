package org.example.umc11th.domain.rental.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.umc11th.domain.book.entity.Book;
import org.example.umc11th.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "rental")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rental_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "rented_at", nullable = false)
    private LocalDateTime rentedAt;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    // 대여 생성: 지금 대여, 7일 뒤 반납 기한
    public static Rental create(User user, Book book) {
        Rental rental = new Rental();
        rental.user = user;
        rental.book = book;
        rental.rentedAt = LocalDateTime.now();
        rental.dueAt = rental.rentedAt.plusDays(7);
        return rental;
    }

    // 반납 처리 (선택 미션)
    public void returnBook() {
        this.returnedAt = LocalDateTime.now();
    }
}
