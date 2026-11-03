package com.example.baby_doyeop.service;

import com.example.baby_doyeop.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;

    public void createRental(Map<String, Object> body) {
        rentalRepository.save(body);
    }

    public boolean returnRental(Long rentalId) {
        return rentalRepository.updateReturnedAt(rentalId) > 0;
    }
}
