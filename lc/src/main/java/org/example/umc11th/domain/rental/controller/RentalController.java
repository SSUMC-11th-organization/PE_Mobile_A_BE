package org.example.umc11th.domain.rental.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.rental.dto.RentalRequest;
import org.example.umc11th.domain.rental.entity.Rental;
import org.example.umc11th.domain.rental.service.RentalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rentals")
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    public String createRental(@RequestBody @Valid RentalRequest rental){
        Long rentalId = rentalService.createRental(rental);
        return "대여가 완료되었습니다.";
    }

    @PatchMapping("/{rentalId}/return")
    public String returnRental(@PathVariable Long rentalId){
        rentalService.returnRental(rentalId);
        return "반납이 완료되었습니다.";
    }
}
