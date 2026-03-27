package com.warroom.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.warroom.dto.AuthResponse;
import com.warroom.dto.GoogleLoginRequest;
import com.warroom.dto.LoginRequest;
import com.warroom.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Mock login for user: {}", request.getEmail());
        AuthResponse response = new AuthResponse("mock-token-" + UUID.randomUUID(), "mock-uid-" + UUID.randomUUID());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        log.info("Mock register for user: {}", request.getEmail());
        AuthResponse response = new AuthResponse("mock-token-" + UUID.randomUUID(), "mock-uid-" + UUID.randomUUID());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        try {
            log.info("Verifying Google ID token...");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.getIdToken());
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            log.info("Google login successful for user: {} (uid: {})", email, uid);

            // In a real app, generate a proper JWT or check database
            AuthResponse response = new AuthResponse("google-token-" + UUID.randomUUID(), uid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Google login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
