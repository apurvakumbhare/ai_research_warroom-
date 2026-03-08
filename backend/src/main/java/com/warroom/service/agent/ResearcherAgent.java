package com.warroom.service.agent;

import com.warroom.exception.WarRoomException;
import com.warroom.util.JsonParserUtil;
import com.warroom.util.PromptTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Agent responsible for deep research and strengthening the technical/market
 * depth.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResearcherAgent implements DebateAgent {

    private final OpenAIClient openAIClient;

    @Override
    public String getAgentRole() {
        return "RESEARCHER";
    }

    @Override
    public String execute(String title, String description, String previousContext) {
        log.info("Executing Researcher Agent for project: {}", title);

        String prompt = PromptTemplates.buildResearcherPrompt(title, description);
        String rawResponse = openAIClient.call(prompt);
        String cleanJson = JsonParserUtil.extractJson(rawResponse);

        // Validate JSON structure
        try {
            JsonParserUtil.parse(cleanJson, Map.class);
        } catch (Exception e) {
            log.error("Invalid JSON from Researcher Agent: {}", cleanJson);
            throw new WarRoomException("AGENT_JSON_INVALID", "Researcher Agent produced invalid JSON");
        }

        log.info("Researcher Agent execution completed.");
        return cleanJson;
    }
}
