package com.warroom.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for AI agent models using Spring AI.
 * Focuses on high-efficiency reasoning using the gpt-4o-mini model.
 */
@Configuration
public class OpenAIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    /**
     * Configures the OpenAiChatModel with optimized settings for debate tokens.
     */
    @Bean
    public OpenAiChatModel openAiChatModel() {
        var openAiApi = new OpenAiApi(apiKey);
        var defaultOptions = OpenAiChatOptions.builder()
                .withModel("gpt-4o-mini")
                .withTemperature(0.7)
                .withMaxTokens(2000)
                .build();

        return new OpenAiChatModel(openAiApi, defaultOptions);
    }

    /**
     * Provides a fluent ChatClient bean for orchestrating agent interactions.
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
