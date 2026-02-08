package com.taskmaster.controller;

import com.taskmaster.service.UserStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserStore userStore;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> user) {
        String email = user.get("email");
        String password = user.get("password");

        if (email == null || !email.contains("@") || password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("message", "Validation failed: Invalid email or password too short"));
        }

        if (userStore.userExists(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email already registered"));
        }

        userStore.addUser(email); // Zapisujemy usera "na niby"
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        // Walidacja: sprawdzamy czy user istnieje w naszym UserStore (lub hardcoded admin)
        boolean isValidUser = userStore.userExists(email) || "admin@demo.local".equals(email);

        if (!isValidUser || password == null || password.isEmpty()) {
            // US-02: Błędne dane zwracają 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
        }

        String fakeJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                         UUID.randomUUID() +
                         ".SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        return ResponseEntity.ok(Map.of(
            "token", fakeJwt,
            "type", "Bearer",
            "email", email
        ));
    }
}
