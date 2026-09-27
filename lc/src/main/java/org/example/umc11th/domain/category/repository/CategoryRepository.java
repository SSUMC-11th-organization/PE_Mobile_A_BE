package org.example.umc11th.domain.category.repository;

import org.example.umc11th.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
