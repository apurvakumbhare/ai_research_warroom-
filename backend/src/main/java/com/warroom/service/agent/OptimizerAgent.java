package com.warroom.service.agent;

import com.warroom.exception.WarRoomException;
import com.warroom.util.JsonParserUtil;
import com.warroom.util.PromptTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Agent responsible for suggesting improvements for scalability and efficiency.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OptimizerAgent implements DebateAgent {

    private final OpenAIClient openAIClient;

    @Override
    public String getAgentRole() {
        return "OPTIMIZER";
    }

    @Override
    public String execute(String title, String description, String previousContext) {
        log.info("Executing Optimizer Agent for project: {}", title);

        String prompt = PromptTemplates.buildOptimizerPrompt(title, description);
        String rawResponse = openAIClient.call(prompt);
        String cleanJson = JsonParserUtil.extractJson(rawResponse);

        try {
            JsonParserUtil.parse(cleanJson, Map.class);
        } catch (Exception e) {
            log.error("Invalid JSON from Optimizer Agent: {}", cleanJson);
            throw new WarRoomException("AGENT_JSON_INVALID", "Optimizer Agent produced invalid JSON");
        }

        log.info("Optimizer Agent execution completed.");
        return cleanJson;
    }
}
