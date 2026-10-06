package org.example.umc11th.domain.rental.dto;

import jakarta.validation.constraints.NotNull;

public record RentalRequestDTO(
        @NotNull Long userId,
        @NotNull Long bookId
){
}
