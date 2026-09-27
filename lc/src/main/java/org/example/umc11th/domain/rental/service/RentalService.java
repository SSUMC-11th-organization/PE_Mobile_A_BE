package org.example.umc11th.domain.rental.service;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.book.entity.Book;
import org.example.umc11th.domain.book.repository.BookRepository;
import org.example.umc11th.domain.rental.dto.RentalRequest;
import org.example.umc11th.domain.rental.entity.Rental;
import org.example.umc11th.domain.rental.repostory.RentalRepository;
import org.example.umc11th.domain.user.entity.User;
import org.example.umc11th.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalService {
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public Long createRental(RentalRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 도서입니다. "));

        if(!book.getIsAvailable()){
            throw new RuntimeException("이미 대여 중인 도서입니다.");
        }
        book.setIsAvailable(false);

        Rental rental = Rental.create(user, book);
        return rentalRepository.save(rental).getId();
    }

    public void returnRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 대여 기록입니다."));

        if (rental.getReturnedAt() != null) {
            throw new RuntimeException("이미 반납된 대여 기록입니다.");
        }

        rental.returnBook();
        rental.getBook().setIsAvailable(true);
    }
}
