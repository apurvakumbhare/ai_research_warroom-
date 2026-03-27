package com.warroom.service.orchestrator;

import com.warroom.dto.WarRoomResult;
import com.warroom.entity.AgentOutput;
import com.warroom.entity.ChatSession;
import com.warroom.entity.Project;
import com.warroom.repository.AgentOutputRepository;
import com.warroom.service.agent.ClaudeClient;
import com.warroom.service.agent.GeminiClient;
import com.warroom.service.agent.GroqClient;
import com.warroom.service.agent.OpenAIClient;
import com.warroom.util.PromptTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Multi-model debate pipeline with round-robin turn-taking.
 * Agents: Strategist (Gemini) → Critic (Claude) → Optimizer (OpenAI) →
 * Architect (Groq/Llama)
 * Runs for configurable number of rounds with document grounding.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DebatePipeline {

    private final GeminiClient geminiClient;
    private final ClaudeClient claudeClient;
    private final OpenAIClient openAIClient;
    private final GroqClient groqClient;
    private final AgentOutputRepository agentOutputRepository;
    private final com.warroom.repository.ChatSessionRepository chatSessionRepository;
    private final com.google.cloud.firestore.Firestore firestore;

    @Value("${warroom.ai.debate.max-rounds:3}")
    private int maxRounds;

    @Value("${warroom.ai.gemini.model:gemini-1.5-pro}")
    private String geminiModel;

    @Value("${warroom.ai.anthropic.model:claude-3-5-sonnet-20241022}")
    private String claudeModel;

    @Value("${warroom.ai.groq.model:llama-3.1-70b-versatile}")
    private String groqModel;

    /**
     * Runs the multi-round debate: each round cycles through all 4 agents.
     * Total messages = maxRounds × 4 agents.
     */
    public WarRoomResult runDebate(Project project, ChatSession session) {
        log.info("Starting multi-model debate pipeline for session: {} ({} rounds)", session.getId(), maxRounds);

        // Fetch extracted document text from uploads (Ground Truth)
        String documentContext = fetchDocumentContext(project);
        String hypothesis = project.getCoreHypothesis();
        String title = project.getProjectName();

        int iteration = 1;
        java.util.Random random = new java.util.Random();

        // Continuous open-chatroom debate loop
        while (true) {
            ChatSession currentSession = chatSessionRepository.findById(session.getId()).block();
            if (currentSession == null) {
                currentSession = session;
            }
            if ("COMPLETED".equals(currentSession.getStatus())) {
                log.info("Chat session {} marked as COMPLETED. Stopping continuous loop.", session.getId());
                break;
            }

            log.info("=== Continuous Turn {} ===", iteration);
            
            // 1. STRATEGIST (Gemini)
            try {
                String history = getFormattedHistory(session.getId());
                String prompt = PromptTemplates.buildStrategistPrompt(title, hypothesis, documentContext, history);
                String resp = geminiClient.call(prompt);
                if (resp != null) saveAgentOutput(session, project, "STRATEGIST", geminiModel, resp, iteration);
            } catch (Exception e) { log.error("Strategist failed: {}", e.getMessage()); }
            sleep(1000);

            // 2. CRITIC (Claude)
            try {
                String history = getFormattedHistory(session.getId());
                String prompt = PromptTemplates.buildCriticPrompt(title, hypothesis, documentContext, history);
                String resp = claudeClient.call(prompt);
                if (resp != null) saveAgentOutput(session, project, "CRITIC", claudeModel, resp, iteration);
            } catch (Exception e) { log.error("Critic failed: {}", e.getMessage()); }
            sleep(1000);

            // 3. OPTIMIZER (OpenAI)
            try {
                String history = getFormattedHistory(session.getId());
                String prompt = PromptTemplates.buildOptimizerPrompt(title, hypothesis, documentContext, history);
                String resp = openAIClient.call(prompt);
                if (resp != null) saveAgentOutput(session, project, "OPTIMIZER", "gpt-4o-mini", resp, iteration);
            } catch (Exception e) { log.error("Optimizer failed: {}", e.getMessage()); }
            sleep(1000);

            // 4. ARCHITECT (Groq/Llama)
            try {
                String history = getFormattedHistory(session.getId());
                String prompt = PromptTemplates.buildArchitectPrompt(title, hypothesis, documentContext, history);
                String resp = groqClient.call(prompt);
                if (resp != null) saveAgentOutput(session, project, "ARCHITECT", groqModel, resp, iteration);
            } catch (Exception e) { log.error("Architect failed: {}", e.getMessage()); }
            
            iteration++;
            // Natural pause between rounds
            sleep(2000);
        }

        // Final Synthesis (using OpenAI)
        log.info("Running final synthesis...");
        String fullHistory = getFormattedHistory(session.getId());
        String synthesisPrompt = PromptTemplates.buildSynthesizerPrompt(fullHistory);
        String synthesisResponse = openAIClient.call(synthesisPrompt);
        saveAgentOutput(session, project, "SYNTHESIZER", "gpt-4o-mini", synthesisResponse, iteration);

        // Build result
        WarRoomResult result = WarRoomResult.builder()
                .projectId(project.getId() != null ? java.util.UUID.fromString(project.getId()) : null)
                .improvedVersion(synthesisResponse)
                .completedAt(LocalDateTime.now())
                .build();

        log.info("Debate pipeline completed for session: {}", session.getId());
        return result;
    }

    /**
     * Fetches extracted text from all uploaded documents for this project.
     */
    private String fetchDocumentContext(Project project) {
        StringBuilder extractedContext = new StringBuilder();
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = firestore
                    .collection("projects").document(project.getId()).collection("uploads")
                    .get().get().getDocuments();
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : documents) {
                if (doc.contains("extractedText") && doc.getString("extractedText") != null) {
                    extractedContext.append("### ").append(doc.getString("fileName")).append("\n")
                            .append(doc.getString("extractedText")).append("\n\n");
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch document context for project {}: {}", project.getId(), e.getMessage());
        }

        // Truncate to ~15k characters to protect prompt token limits
        if (extractedContext.length() > 15000) {
            log.info("Truncating document context for project {} (original length: {})", project.getId(),
                    extractedContext.length());
            return extractedContext.substring(0, 15000) + "... [Truncated for prompt limit]";
        }
        return extractedContext.toString();
    }

    private void saveAgentOutput(ChatSession session, Project project, String role, String modelName,
            String output, int iteration) {

        String finalOutput = (output == null || output.trim().isEmpty())
                ? "[The AI agent returned an empty or whitespace-only response. This often happens due to content safety filters or token limit refusals.]"
                : output;

        AgentOutput agentOutput = AgentOutput.builder()
                .chatSessionId(session.getId())
                .projectId(project.getId())
                .agentName(role)
                .modelName(modelName)
                .output(finalOutput)
                .iteration(iteration)
                .generatedAt(LocalDateTime.now().toString())
                .build();
        agentOutputRepository.save(agentOutput);
        log.debug("Saved {} ({}) output for round {}", role, modelName, iteration);
    }

    private String getFormattedHistory(String sessionId) {
        List<AgentOutput> history = agentOutputRepository
                .findByChatSessionIdOrderByGeneratedAtAsc(sessionId);
        if (history == null || history.isEmpty())
            return "";

        int startIdx = Math.max(0, history.size() - 10);
        List<AgentOutput> recentHistory = history.subList(startIdx, history.size());

        return recentHistory.stream()
                .map(o -> o.getAgentName() + " [" + (o.getModelName() != null ? o.getModelName() : "unknown") + "]: "
                        + o.getOutput())
                .collect(Collectors.joining("\n\n"));
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
