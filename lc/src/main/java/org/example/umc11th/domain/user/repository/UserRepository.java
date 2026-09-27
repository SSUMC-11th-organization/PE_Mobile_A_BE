package org.example.umc11th.domain.user.repository;

import org.example.umc11th.domain.user.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
