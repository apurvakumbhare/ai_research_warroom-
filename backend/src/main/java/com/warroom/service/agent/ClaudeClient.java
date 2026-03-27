package com.warroom.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * REST client for Claude via OpenRouter (openrouter.ai).
 * Uses OpenAI-compatible /chat/completions endpoint.
 * Used by Agent B: The Critic (Con perspective).
 */
@Slf4j
@Service
public class ClaudeClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;

    // OpenRouter uses the OpenAI-compatible chat completions format
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    public ClaudeClient(RestTemplate restTemplate,
                        @Value("${warroom.ai.anthropic.api-key}") String apiKey,
                        @Value("${warroom.ai.anthropic.model}") String model) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * Calls Claude via OpenRouter and returns the text response.
     * OpenRouter uses OpenAI-compatible format: { choices[].message.content }
     */
    public String call(String prompt) {
        log.debug("Calling Claude via OpenRouter ({}) with prompt length: {}", model, prompt.length());

        // OpenRouter uses the OpenAI chat completions format
        Map<String, Object> userMessage = Map.of("role", "user", "content", prompt);
        Map<String, Object> body = Map.of(
                "model", "anthropic/claude-3.5-sonnet",   // OpenRouter model identifier
                "max_tokens", 500,
                "messages", List.of(userMessage)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "https://ai-research-warroom.app");
        headers.set("X-Title", "AI Research War-Room");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            if (response.getBody() != null) {
                // OpenAI-compatible response: choices[0].message.content
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }
            log.warn("Claude (OpenRouter) returned empty response. Body: {}", response.getBody());
            return "[Claude returned no content]";
        } catch (Exception e) {
            log.error("Failed to call Claude via OpenRouter: {}", e.getMessage());
            return "[Claude API error: " + e.getMessage() + "]";
        }
    }
}
