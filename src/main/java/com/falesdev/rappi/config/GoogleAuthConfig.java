package com.falesdev.rappi.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class GoogleAuthConfig {

    @Value("${google.client.android.id}")
    private String androidClientId;

    @Value("${google.client.web.id}")
    private String webClientId;

    private List<String> clientIds;

    @PostConstruct
    public void init() {
        clientIds = Arrays.asList(androidClientId, webClientId);
    }

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        if (clientIds == null || clientIds.isEmpty()) {
            throw new IllegalStateException("Google client IDs not configured");
        }
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(clientIds)
                .build();
    }
}
