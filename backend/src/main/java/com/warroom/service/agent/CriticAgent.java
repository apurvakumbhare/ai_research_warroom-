package com.warroom.service.agent;

import com.warroom.exception.WarRoomException;
import com.warroom.util.JsonParserUtil;
import com.warroom.util.PromptTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Agent responsible for identifying logical flaws and questioning assumptions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CriticAgent implements DebateAgent {

    private final OpenAIClient openAIClient;

    @Override
    public String getAgentRole() {
        return "CRITIC";
    }

    @Override
    public String execute(String title, String description, String previousContext) {
        log.info("Executing Critic Agent for project: {}", title);

        String prompt = PromptTemplates.buildCriticPrompt(title, description);
        String rawResponse = openAIClient.call(prompt);
        String cleanJson = JsonParserUtil.extractJson(rawResponse);

        try {
            JsonParserUtil.parse(cleanJson, Map.class);
        } catch (Exception e) {
            log.error("Invalid JSON from Critic Agent: {}", cleanJson);
            throw new WarRoomException("AGENT_JSON_INVALID", "Critic Agent produced invalid JSON");
        }

        log.info("Critic Agent execution completed.");
        return cleanJson;
    }
}
