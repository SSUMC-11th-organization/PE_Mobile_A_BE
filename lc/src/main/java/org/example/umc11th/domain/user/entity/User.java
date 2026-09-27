package org.example.umc11th.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")   // user는 예약어라서 테이블명이 users
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // AUTO_INCREMENT
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 30)
    private String nickname;
}
