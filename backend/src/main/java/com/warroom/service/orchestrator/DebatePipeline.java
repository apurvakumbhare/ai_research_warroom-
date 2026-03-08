package com.warroom.service.orchestrator;

import com.warroom.dto.AgentResponse;
import com.warroom.dto.WarRoomResult;
import com.warroom.entity.AgentOutput;
import com.warroom.entity.DebateSession;
import com.warroom.entity.Project;
import com.warroom.repository.AgentOutputRepository;
import com.warroom.service.agent.OpenAIClient;
import com.warroom.util.Constants;
import com.warroom.util.JsonParserUtil;
import com.warroom.util.PromptTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Execution engine for the AI agent debate pipeline.
 * Responsible for sequential calling of specialized agents and data extraction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DebatePipeline {

    private final OpenAIClient openAIClient;
    private final AgentOutputRepository agentOutputRepository;

    /**
     * Runs the sequential debate chain: Researcher -> Critic -> Optimizer ->
     * Devil's Advocate -> Summarizer.
     * 
     * @param project the project content to analyze
     * @param session the current execution session
     * @return the final consolidated WarRoomResult
     */
    public WarRoomResult runDebate(Project project, DebateSession session) {
        log.info("Starting sequential agent pipeline for session: {}", session.getId());

        // 1. Researcher Phase
        String researcherPrompt = PromptTemplates.buildStrategistPrompt(project.getTitle(), project.getDescription());
        String researcherRaw = openAIClient.call(researcherPrompt);
        saveAgentOutput(session, project, "RESEARCHER", researcherRaw, 1);

        // 2. Critic Phase
        String criticPrompt = PromptTemplates.buildCriticPrompt(project.getTitle(), researcherRaw);
        String criticRaw = openAIClient.call(criticPrompt);
        saveAgentOutput(session, project, "CRITIC", criticRaw, 1);

        // 3. Optimizer Phase
        // Note: Optimizer and Devil's Advocate prompts would typically be built
        // similarly via templates
        String optimizerRaw = openAIClient.call("Optimize this: " + researcherRaw + " given critique: " + criticRaw);
        saveAgentOutput(session, project, "OPTIMIZER", optimizerRaw, 1);

        // 4. Devil's Advocate Phase
        String devilRaw = openAIClient.call("Find hidden risks in: " + optimizerRaw);
        saveAgentOutput(session, project, "DEVIL", devilRaw, 1);

        // 5. Final Synthesis
        StringBuilder debateHistory = new StringBuilder()
                .append("Strategy: ").append(researcherRaw).append("\n")
                .append("Critique: ").append(criticRaw).append("\n")
                .append("Optimization: ").append(optimizerRaw).append("\n")
                .append("Devil's Advocate: ").append(devilRaw);

        String synthesizerPrompt = PromptTemplates.buildSynthesizerPrompt(debateHistory.toString());
        String finalRaw = openAIClient.call(synthesizerPrompt);

        // Extract structured JSON and map to DTO
        String cleanJson = JsonParserUtil.extractJson(finalRaw);
        WarRoomResult result = JsonParserUtil.parse(cleanJson, WarRoomResult.class);

        // Finalize DTO metadata
        result.setProjectId(project.getId() != null ? java.util.UUID.fromString(project.getId()) : null);
        result.setCompletedAt(LocalDateTime.now());

        log.info("Debate pipeline execution finished for session: {}", session.getId());
        return result;
    }

    private void saveAgentOutput(DebateSession session, Project project, String role, String output, int iteration) {
        AgentOutput agentOutput = AgentOutput.builder()
                .debateSessionId(session.getId())
                .projectId(project.getId())
                .agentName(role)
                .output(output)
                .iteration(iteration)
                .generatedAt(LocalDateTime.now())
                .build();
        agentOutputRepository.save(agentOutput);
        log.debug("Saved {} output for iteration {}", role, iteration);
    }
}
