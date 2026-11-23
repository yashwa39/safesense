package com.safesense.controller;

import com.safesense.config.AuthInterceptor;
import com.safesense.model.User;
import com.safesense.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/login")
    public Map<String, String> login(@RequestBody LoginRequest request, HttpSession session) {
        User user = authService.login(request.email(), request.password());
        Map<String, String> publicUser = publicUser(user);
        session.setAttribute(AuthInterceptor.SESSION_USER, publicUser);
        return publicUser;
    }

    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/me")
    public Map<String, String> me(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute(AuthInterceptor.SESSION_USER);
        return user;
    }

    private static Map<String, String> publicUser(User user) {
        return Map.of(
                "name", user.getName(),
                "firstName", user.getFirstName(),
                "email", user.getEmail(),
                "role", user.getRole()
        );
    }

    public record LoginRequest(String email, String password) {
    }
}
