package com.falesdev.rappi.domain.redis;

import com.falesdev.rappi.domain.OtpType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@RedisHash("otp")
@Getter
@Setter
@NoArgsConstructor
public class Otp {
    @Id
    private String key; // Email o teléfono
    private String code;
    private OtpType type;
    private String target;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long ttl = 300L; // 5 minutos

    public Otp(String key, String code,OtpType type,String target) {
        this.key = key;
        this.code = code;
        this.type = type;
        this.target = target;
    }
}
