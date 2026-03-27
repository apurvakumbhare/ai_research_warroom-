package com.warroom.controller;

import com.warroom.dto.AuthResponse;
import com.warroom.dto.LoginRequest;
import com.warroom.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
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
}
