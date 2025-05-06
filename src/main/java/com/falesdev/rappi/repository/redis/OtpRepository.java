package com.falesdev.rappi.repository.redis;

import com.falesdev.rappi.domain.OtpType;
import com.falesdev.rappi.domain.redis.Otp;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends CrudRepository<Otp, String> {

    default Optional<Otp> findByTypeAndTarget(OtpType type, String target) {
        String key = type.name() + ":" + target;
        return findById(key);
    }
}
