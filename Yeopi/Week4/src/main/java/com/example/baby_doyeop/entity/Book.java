package com.example.baby_doyeop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    // ERD의 category 1 : N book 관계. FK 숫자 대신 Category 객체로 들고 다닙니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // [선택 3] 같은 제목 중복 등록 방지 (DB에도 UNIQUE 제약 추가 필요)
    @Column(nullable = false, length = 100, unique = true)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    public Book(Category category, String title, String description) {
        this.category = category;
        this.title = title;
        this.description = description;
    }
}
