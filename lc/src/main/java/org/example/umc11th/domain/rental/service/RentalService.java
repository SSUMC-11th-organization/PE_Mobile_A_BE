package org.example.umc11th.domain.rental.service;

import lombok.RequiredArgsConstructor;
import org.example.umc11th.domain.book.entity.Book;
import org.example.umc11th.domain.book.repository.BookRepository;
import org.example.umc11th.domain.rental.dto.RentalRequest;
import org.example.umc11th.domain.rental.entity.Rental;
import org.example.umc11th.domain.rental.repository.RentalRepository;
import org.example.umc11th.domain.user.entity.User;
import org.example.umc11th.domain.user.repository.UserRepository;
import org.example.umc11th.domain.book.exception.BookException;
import org.example.umc11th.domain.book.exception.BookErrorCode;
import org.example.umc11th.domain.rental.exception.RentalException;
import org.example.umc11th.domain.rental.exception.RentalErrorCode;
import org.example.umc11th.domain.user.exception.UserException;
import org.example.umc11th.domain.user.exception.UserErrorCode;
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
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BookException(BookErrorCode.BOOK_NOT_FOUND));

        if(!book.getIsAvailable()){
            throw new BookException(BookErrorCode.BOOK_ALREADY_RENTED);
        }
        book.setIsAvailable(false);

        Rental rental = Rental.create(user, book);
        return rentalRepository.save(rental).getId();
    }

    public void returnRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalException(RentalErrorCode.RENTAL_NOT_FOUND));

        if (rental.getReturnedAt() != null) {
            throw new RentalException(RentalErrorCode.RENTAL_ALREADY_RETURNED);
        }

        rental.returnBook();
        rental.getBook().setIsAvailable(true);
    }
}
