package com.warroom.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * REST client for Google Gemini API (generativelanguage.googleapis.com).
 * Used by Agent A: The Strategist (Pro perspective).
 */
@Slf4j
@Service
public class GeminiClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;

    public GeminiClient(RestTemplate restTemplate,
                        @Value("${warroom.ai.gemini.api-key}") String apiKey,
                        @Value("${warroom.ai.gemini.model}") String model) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * Calls Gemini with the given prompt and returns the text response.
     */
    public String call(String prompt) {
        log.debug("Calling Gemini ({}) with prompt length: {}", model, prompt.length());

        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                model, apiKey);

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> body = Map.of(
                "contents", List.of(content),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 500
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> contentResp = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResp.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            log.warn("Gemini returned empty response");
            return "[Gemini returned no content]";
        } catch (Exception e) {
            log.error("Failed to call Gemini: {}", e.getMessage());
            return "[Gemini API error: " + e.getMessage() + "]";
        }
    }
}
