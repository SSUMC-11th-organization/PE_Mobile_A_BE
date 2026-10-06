package org.example.umc11th.domain.rental.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.rental.dto.RentalRequestDTO;
import org.example.umc11th.domain.rental.service.RentalService;
import org.example.umc11th.global.apiPayload.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rentals")
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    public ApiResponse<String> createRental(@RequestBody @Valid RentalRequestDTO rental){
        Long rentalId = rentalService.createRental(rental);
        return ApiResponse.onSuccess("대여가 완료되었습니다.");
    }

    @PatchMapping("/{rentalId}/return")
    public ApiResponse<String> returnRental(@PathVariable Long rentalId){
        rentalService.returnRental(rentalId);
        return ApiResponse.onSuccess("반납이 완료되었습니다.");
    }
}
