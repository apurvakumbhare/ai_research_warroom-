package com.warroom.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility for thread-safe JSON parsing and cleaning AI responses.
 * Specifically handles LLM common practice of wrapping JSON in markdown blocks.
 */
public final class JsonParserUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // Support for LocalDateTime

    private JsonParserUtil() {
        // Prevent instantiation
    }

    /**
     * Parses a clean JSON string into a target class.
     */
    public static <T> T parse(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON into " + clazz.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Extracts and cleans JSON content from a raw AI response.
     * Removes markdown code fences (```json ... ```) if present.
     */
    public static String extractJson(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new RuntimeException("AI response is empty");
        }

        String cleaned = rawResponse.trim();

        // Handle markdown block wrapping
        if (cleaned.contains("```json")) {
            cleaned = cleaned.split("```json")[1].split("```")[0].trim();
        } else if (cleaned.contains("```")) {
            cleaned = cleaned.split("```")[1].split("```")[0].trim();
        }

        return cleaned;
    }
}
