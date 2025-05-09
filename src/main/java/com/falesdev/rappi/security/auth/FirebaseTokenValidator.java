package com.falesdev.rappi.security.auth;

import com.falesdev.rappi.exception.AuthenticationException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FirebaseTokenValidator {

    public FirebaseToken validate(String firebaseToken) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
        } catch (FirebaseAuthException e) {
            throw new AuthenticationException("Firebase Token invalid", e);
        }
    }

    public String getPhoneNumber(FirebaseToken decodedToken) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(decodedToken.getUid());
            return userRecord.getPhoneNumber();
        } catch (FirebaseAuthException e) {
            throw new AuthenticationException("Error getting user's phone number", e);
        }
    }

    public String getPhoneNumberFromToken(String firebaseToken) {
        FirebaseToken decodedToken = validate(firebaseToken);
        return getPhoneNumber(decodedToken);
    }
}
