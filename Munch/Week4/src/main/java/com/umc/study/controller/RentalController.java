package com.umc.study.controller;

import com.umc.study.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createRental(@RequestBody Map<String, Object> body) {
        rentalService.createRental(body);

        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 Created
                .body(Map.of("message", "대여 기록이 생성되었습니다!"));
    }

    @PatchMapping("/{rentalId}/return")
    public ResponseEntity<Map<String, Object>> returnRental(@PathVariable("rentalId") Long rentalId) {
        boolean success = rentalService.returnRental(rentalId);

        if (!success) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND) // 404 Not Found
                    .body(Map.of("message", "해당 대여 기록이 존재하지 않습니다."));
        }

        return ResponseEntity
                .ok(Map.of("message", "반납 처리가 완료되었습니다!")); // 200 OK
    }
}
