package com.example.baby_doyeop.repository;

import com.example.baby_doyeop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
