package com.warroom.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * REST client for Anthropic Claude API (api.anthropic.com).
 * Used by Agent B: The Critic (Con perspective).
 */
@Slf4j
@Service
public class ClaudeClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;

    private static final String API_URL = "https://api.anthropic.com/v1/messages";

    public ClaudeClient(RestTemplate restTemplate,
                        @Value("${warroom.ai.anthropic.api-key}") String apiKey,
                        @Value("${warroom.ai.anthropic.model}") String model) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * Calls Claude with the given prompt and returns the text response.
     */
    public String call(String prompt) {
        log.debug("Calling Claude ({}) with prompt length: {}", model, prompt.length());

        Map<String, Object> userMessage = Map.of("role", "user", "content", prompt);
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 3000,
                "messages", List.of(userMessage)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
                if (content != null && !content.isEmpty()) {
                    return (String) content.get(0).get("text");
                }
            }
            log.warn("Claude returned empty response");
            return "[Claude returned no content]";
        } catch (Exception e) {
            log.error("Failed to call Claude: {}", e.getMessage());
            return "[Claude API error: " + e.getMessage() + "]";
        }
    }
}
