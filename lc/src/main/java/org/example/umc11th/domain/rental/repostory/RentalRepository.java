package org.example.umc11th.domain.rental.repostory;

import org.example.umc11th.domain.rental.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {
}
