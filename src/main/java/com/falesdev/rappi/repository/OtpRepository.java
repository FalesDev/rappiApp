package com.falesdev.rappi.repository;

import com.falesdev.rappi.domain.redis.Otp;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends CrudRepository<Otp, String> {
    Optional<Otp> findByKey(String key);
}
