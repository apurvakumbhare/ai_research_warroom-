package com.warroom.service.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for normalizing and cleaning text content.
 * Ensures that the input for AI agents is concise and free of artifacts.
 */
@Slf4j
@Service
public class TextCleanerService {

    /**
     * Cleans the provided text by removing excessive whitespace, newlines,
     * and non-printable characters.
     * 
     * @param rawText the text to clean
     * @return the normalized text
     */
    public String clean(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        log.debug("Cleaning text of length: {}", rawText.length());

        // Normalize newlines and tabs
        String cleaned = rawText.replaceAll("\\r\\n|\\r|\\n", " ");

        // Remove non-printable characters
        cleaned = cleaned.replaceAll("[\\p{C}]", "");

        // Normalize spaces (multiple spaces to single)
        cleaned = cleaned.replaceAll("\\s+", " ");

        cleaned = cleaned.trim();

        log.debug("Text cleaned successfully. Original: {} -> New: {}", rawText.length(), cleaned.length());

        return cleaned;
    }
}
