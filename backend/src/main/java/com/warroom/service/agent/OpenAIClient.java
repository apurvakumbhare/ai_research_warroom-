package com.warroom.service.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Client service for interacting with OpenAI via Spring AI's ChatClient.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIClient {

    private final ChatClient chatClient;

    /**
     * Executes a call to the LLM with the provided prompt.
     * 
     * @param prompt the instructions for the agent
     * @return the raw string response from the AI
     */
    public String call(String prompt) {
        log.debug("Calling OpenAI with prompt length: {}", prompt.length());

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Failed to call OpenAI: {}", e.getMessage());
            throw new RuntimeException("AI_SERVICE_UNAVAILABLE", e);
        }
    }
}
