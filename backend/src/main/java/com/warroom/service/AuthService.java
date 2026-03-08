package com.warroom.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.warroom.dto.AuthResponse;
import com.warroom.dto.LoginRequest;
import com.warroom.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthService {

    @Value("${firebase.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final FirebaseAuth firebaseAuth;

    public AuthService(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public AuthResponse register(RegisterRequest request) throws Exception {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + apiKey;

        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("email", request.getEmail());
        reqBody.put("password", request.getPassword());
        reqBody.put("returnSecureToken", true);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, reqBody, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("idToken")) {
                throw new Exception("Failed to get ID token from Firebase");
            }

            String idToken = (String) body.get("idToken");
            String localId = (String) body.get("localId"); // The uid

            // Write user details to Firestore matching the perfected schema
            Firestore db = FirestoreClient.getFirestore();
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", request.getEmail());
            userData.put("fullName", request.getName());
            userData.put("role", request.getRole());

            // Convert dob (String "1990-01-01") to Timestamp at midnight UTC
            LocalDateTime dobDateTime = LocalDateTime.parse(request.getDob() + "T00:00:00");
            userData.put("dob", Timestamp.ofTimeSecondsAndNanos(dobDateTime.toEpochSecond(ZoneOffset.UTC), 0));
            userData.put("createdAt", Timestamp.now());

            db.collection("users").document(localId).set(userData).get(); // blocking wait
            log.info("Successfully registered and provisioned user in Firestore: {}", localId);

            return new AuthResponse(idToken, localId, "Success");
        } catch (Exception e) {
            log.error("Registration failed", e);
            throw new Exception("Registration failed: " + e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) throws Exception {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;

        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("email", request.getEmail());
        reqBody.put("password", request.getPassword());
        reqBody.put("returnSecureToken", true);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, reqBody, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("idToken")) {
                throw new Exception("Failed to get ID token from Firebase");
            }

            String idToken = (String) body.get("idToken");
            String localId = (String) body.get("localId");
            log.info("Successfully logged in user: {}", localId);

            return new AuthResponse(idToken, localId, "Success");
        } catch (Exception e) {
            log.error("Login failed", e);
            throw new Exception("Invalid email or password");
        }
    }
}
