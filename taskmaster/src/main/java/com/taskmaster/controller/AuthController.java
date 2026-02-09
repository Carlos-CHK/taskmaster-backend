package com.taskmaster.controller;

import com.taskmaster.controller.dto.AuthRequest;
import com.taskmaster.controller.dto.AuthResponse;
import com.taskmaster.model.User;
import com.taskmaster.repository.UserRepository;
import com.taskmaster.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        String email = request.email();
        String password = request.password();

        if (email == null || !email.contains("@") || password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("message", "Validation failed: Invalid email or password too short"));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email already registered"));
        }

        User user = new User(null, email, passwordEncoder.encode(password));
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String email = request.email();
        String password = request.password();

        if (email == null || !email.contains("@") || password == null || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            // US-02: Błędne dane zwracają 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
        }

        String jwt = jwtService.generateToken(email);
        return ResponseEntity.ok(new AuthResponse(jwt, "Bearer", email));
    }
}
