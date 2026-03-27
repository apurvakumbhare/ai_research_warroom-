package com.warroom.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * REST client for Groq API (api.groq.com) — OpenAI-compatible endpoint.
 * Used by Agent D: The Architect (Technical perspective).
 * Runs Llama 3.1 70B.
 */
@Slf4j
@Service
public class GroqClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    public GroqClient(RestTemplate restTemplate,
                      @Value("${warroom.ai.groq.api-key}") String apiKey,
                      @Value("${warroom.ai.groq.model}") String model) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * Calls Groq (Llama) with the given prompt and returns the text response.
     */
    public String call(String prompt) {
        log.debug("Calling Groq/Llama ({}) with prompt length: {}", model, prompt.length());

        Map<String, Object> userMessage = Map.of("role", "user", "content", prompt);
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(userMessage),
                "max_tokens", 3000,
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }
            log.warn("Groq returned empty response");
            return "[Groq/Llama returned no content]";
        } catch (Exception e) {
            log.error("Failed to call Groq: {}", e.getMessage());
            return "[Groq API error: " + e.getMessage() + "]";
        }
    }
}
