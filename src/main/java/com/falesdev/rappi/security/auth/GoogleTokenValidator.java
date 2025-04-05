package com.falesdev.rappi.security.auth;

import com.falesdev.rappi.exception.AuthenticationException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
@RequiredArgsConstructor
public class GoogleTokenValidator {

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdToken.Payload validate(String idToken) {
        try {
            GoogleIdToken idTokenObj = verifier.verify(idToken);
            if (idTokenObj == null) {
                throw new AuthenticationException("Invalid or expired token");
            }

            GoogleIdToken.Payload payload = idTokenObj.getPayload();

            if (!payload.getIssuer().equals("https://accounts.google.com")
                    && !payload.getIssuer().equals("accounts.google.com")) {
                throw new AuthenticationException("Invalid token issuer");
            }

            return payload;

        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Token malformation", e);
        } catch (GeneralSecurityException | IOException e) {
            throw new AuthenticationException("Crypto verification error", e);
        }
    }
}
