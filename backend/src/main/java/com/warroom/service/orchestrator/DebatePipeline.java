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
 * Agents: Strategist (Gemini) → Critic (Claude) → Optimizer (OpenAI) → Architect (Groq/Llama)
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
            String history = getFormattedHistory(session.getId());

            int randomAgent = random.nextInt(4);

            if (randomAgent == 0) {
                log.info("Turn {} - Agent A: Strategist (Gemini) responding...", iteration);
                String strategistPrompt = PromptTemplates.buildStrategistPrompt(title, hypothesis, documentContext, history);
                String strategistResponse = geminiClient.call(strategistPrompt);
                saveAgentOutput(session, project, "STRATEGIST", geminiModel, strategistResponse, iteration);
            } else if (randomAgent == 1) {
                log.info("Turn {} - Agent B: Critic (Claude) responding...", iteration);
                String criticPrompt = PromptTemplates.buildCriticPrompt(title, hypothesis, documentContext, history);
                String criticResponse = claudeClient.call(criticPrompt);
                saveAgentOutput(session, project, "CRITIC", claudeModel, criticResponse, iteration);
            } else if (randomAgent == 2) {
                log.info("Turn {} - Agent C: Optimizer (OpenAI) responding...", iteration);
                String optimizerPrompt = PromptTemplates.buildOptimizerPrompt(title, hypothesis, documentContext, history);
                String optimizerResponse = openAIClient.call(optimizerPrompt);
                saveAgentOutput(session, project, "OPTIMIZER", "gpt-4o-mini", optimizerResponse, iteration);
            } else {
                log.info("Turn {} - Agent D: Architect (Groq/Llama) responding...", iteration);
                String architectPrompt = PromptTemplates.buildArchitectPrompt(title, hypothesis, documentContext, history);
                String architectResponse = groqClient.call(architectPrompt);
                saveAgentOutput(session, project, "ARCHITECT", groqModel, architectResponse, iteration);
            }

            iteration++;
            // Random delay between 4 to 8 seconds to pace the debate and allow user interaction
            sleep(4000 + random.nextInt(4000));
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
        return extractedContext.toString();
    }

    private void saveAgentOutput(ChatSession session, Project project, String role, String modelName,
            String output, int iteration) {
        AgentOutput agentOutput = AgentOutput.builder()
                .chatSessionId(session.getId())
                .projectId(project.getId())
                .agentName(role)
                .modelName(modelName)
                .output(output)
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
