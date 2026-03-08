package com.warroom.service.agent;

import com.warroom.exception.WarRoomException;
import com.warroom.util.JsonParserUtil;
import com.warroom.util.PromptTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Agent responsible for exploring worst-case failure scenarios.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DevilsAdvocateAgent implements DebateAgent {

    private final OpenAIClient openAIClient;

    @Override
    public String getAgentRole() {
        return "DEVIL";
    }

    @Override
    public String execute(String title, String description, String previousContext) {
        log.info("Executing Devil's Advocate Agent for project: {}", title);

        String prompt = PromptTemplates.buildDevilPrompt(title, description);
        String rawResponse = openAIClient.call(prompt);
        String cleanJson = JsonParserUtil.extractJson(rawResponse);

        try {
            JsonParserUtil.parse(cleanJson, Map.class);
        } catch (Exception e) {
            log.error("Invalid JSON from Devil's Advocate Agent: {}", cleanJson);
            throw new WarRoomException("AGENT_JSON_INVALID", "Devil's Advocate Agent produced invalid JSON");
        }

        log.info("Devil's Advocate Agent execution completed.");
        return cleanJson;
    }
}
