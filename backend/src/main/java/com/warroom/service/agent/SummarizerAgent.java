package com.warroom.service.agent;

import com.warroom.exception.WarRoomException;
import com.warroom.util.JsonParserUtil;
import com.warroom.util.PromptTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Agent responsible for synthesizing the entire debate into a final structured
 * roadmap.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SummarizerAgent implements DebateAgent {

    private final OpenAIClient openAIClient;

    @Override
    public String getAgentRole() {
        return "SUMMARIZER";
    }

    @Override
    public String execute(String title, String description, String debateHistory) {
        log.info("Executing Summarizer Agent for project: {}", title);

        String prompt = PromptTemplates.buildSynthesizerPrompt(debateHistory);
        String rawResponse = openAIClient.call(prompt);
        String cleanJson = JsonParserUtil.extractJson(rawResponse);

        try {
            JsonParserUtil.parse(cleanJson, Map.class);
        } catch (Exception e) {
            log.error("Invalid JSON from Summarizer Agent: {}", cleanJson);
            throw new WarRoomException("AGENT_JSON_INVALID", "Summarizer Agent produced invalid JSON");
        }

        log.info("Summarizer Agent execution completed.");
        return cleanJson;
    }
}
