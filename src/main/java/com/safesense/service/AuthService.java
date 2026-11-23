package com.safesense.service;

import com.safesense.exception.AuthenticationException;
import com.safesense.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Single demo user. Password is stored as a BCrypt hash (starts with $2a$).
 */
@Service
public class AuthService {

    public static final String DEMO_EMAIL = "aisha@demo.com";
    public static final String DEMO_PASSWORD = "Demo@123";

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final User demoUser;

    public AuthService() {
        this.demoUser = new User(
                "Aisha Khan",
                DEMO_EMAIL,
                encoder.encode(DEMO_PASSWORD),
                "Supervisor"
        );
    }

    public User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationException("Invalid email or password");
        }
        boolean emailOk = demoUser.getEmail().equalsIgnoreCase(email.trim());
        boolean passwordOk = encoder.matches(password, demoUser.getPasswordHash());
        if (!emailOk || !passwordOk) {
            throw new AuthenticationException("Invalid email or password");
        }
        return demoUser;
    }

    public User getDemoUser() {
        return demoUser;
    }

    public String getPasswordHashPrefix() {
        return demoUser.getPasswordHash().substring(0, Math.min(4, demoUser.getPasswordHash().length()));
    }
}
