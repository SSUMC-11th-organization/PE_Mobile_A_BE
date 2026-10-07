package com.example.baby_doyeop.controller;

import com.example.baby_doyeop.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // POST http://localhost:8080/rentals
    @PostMapping
    public ResponseEntity<String> createRental(@RequestBody Map<String, Object> body) {
        rentalService.createRental(body);
        return ResponseEntity.status(HttpStatus.CREATED).body("도서 대여가 완료되었습니다!");
    }

    // PATCH http://localhost:8080/rentals/{rentalId}/return
    @PatchMapping("/{rentalId}/return")
    public ResponseEntity<String> returnRental(@PathVariable Long rentalId) {
        if (!rentalService.returnRental(rentalId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 대여 기록입니다.");
        }
        return ResponseEntity.ok("도서 반납이 완료되었습니다!");
    }
}
