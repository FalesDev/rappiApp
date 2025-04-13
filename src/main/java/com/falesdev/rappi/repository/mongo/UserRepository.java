package com.falesdev.rappi.repository.mongo;

import com.falesdev.rappi.domain.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByPhone(String phone);
}
