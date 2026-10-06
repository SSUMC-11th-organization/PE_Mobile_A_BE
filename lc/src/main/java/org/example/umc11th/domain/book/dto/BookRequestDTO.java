package org.example.umc11th.domain.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookRequestDTO(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 100) String title,
        String description
) {
}