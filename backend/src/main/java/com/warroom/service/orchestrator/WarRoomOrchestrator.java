package com.warroom.service.orchestrator;

import com.warroom.dto.AgentMessage;
import com.warroom.dto.DebateResponse;
import com.warroom.dto.WarRoomResult;
import com.warroom.entity.AgentOutput;
import com.warroom.entity.ChatSession;
import com.warroom.entity.Project;
import com.warroom.exception.WarRoomException;
import com.warroom.repository.AgentOutputRepository;
import com.warroom.repository.ChatSessionRepository;
import com.warroom.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main orchestrator for the AI Research War-Room.
 * Manages the lifecycle of a multi-agent debate session with 4 distinct AI models.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarRoomOrchestrator {

    private final ProjectRepository projectRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final AgentOutputRepository agentOutputRepository;
    private final DebatePipeline debatePipeline;
    private final com.warroom.service.agent.OpenAIClient openAIClient;
    private final java.util.concurrent.Executor taskExecutor;

    /**
     * Executes the full multi-agent debate for a project.
     */
    public WarRoomResult executeWarRoom(UUID projectId) {
        log.info("Initiating War-Room orchestration for project: {}", projectId);

        Project project = projectRepository.findById(projectId.toString())
                .orElseThrow(() -> new WarRoomException("PROJECT_NOT_FOUND",
                        "Project not found with ID: " + projectId));

        List<ChatSession> sessions = chatSessionRepository.findByProjectId(projectId.toString()).collectList().block();
        ChatSession session = (sessions != null && !sessions.isEmpty()) ? sessions.get(sessions.size() - 1) : null;
        
        if (session == null) {
            session = ChatSession.builder()
                    .projectId(project.getId())
                    .status("STARTED")
                    .isEnded(false)
                    .createdAt(new Date())
                    .build();
            session = chatSessionRepository.save(session).block();
            log.debug("Created chat session: {}", session.getId());
        }

        try {
            session.setStatus("IN_PROGRESS");
            chatSessionRepository.save(session).block();

            WarRoomResult result = debatePipeline.runDebate(project, session);

            project.setFinalContent(result.getImprovedVersion());
            project.setStatus("COMPLETED");
            projectRepository.save(project);

            session.setStatus("COMPLETED");
            session.setEnded(true);
            session.setUpdatedAt(new Date());
            chatSessionRepository.save(session).block();

            log.info("Orchestration completed successfully for project: {}", projectId);
            return result;

        } catch (Exception ex) {
            log.error("Orchestration failed for project {}: {}", projectId, ex.getMessage());
            session.setStatus("FAILED");
            session.setEnded(true);
            chatSessionRepository.save(session).block();
            throw new WarRoomException("ORCHESTRATION_FAILED",
                    "Failed to execute AI debate: " + ex.getMessage());
        }
    }

    /**
     * Entry point — starts orchestration asynchronously.
     */
    public WarRoomResult startOrchestration(UUID projectId) {
        log.info("startOrchestration called for project: {}", projectId);
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            log.info("Async task launched for project: {}", projectId);
            try {
                executeWarRoom(projectId);
            } catch (Exception e) {
                log.error("Async execution failed for project {}", projectId, e);
            }
        }, taskExecutor);

        return WarRoomResult.builder().build();
    }

    /**
     * Retrieves the current status and messages of a debate session.
     */
    public DebateResponse getOrchestrationStatus(UUID projectId) {
        List<ChatSession> sessions = chatSessionRepository.findByProjectId(projectId.toString()).collectList().block();
        ChatSession session = (sessions != null && !sessions.isEmpty()) ? sessions.get(sessions.size() - 1) : null;

        if (session == null) {
            return DebateResponse.builder()
                    .projectId(projectId)
                    .status("NOT_STARTED")
                    .message("No debate session found")
                    .build();
        }

        List<AgentMessage> messages = new ArrayList<>();
        List<AgentOutput> outputs = agentOutputRepository
                .findByChatSessionIdOrderByGeneratedAtAsc(session.getId());

        for (AgentOutput output : outputs) {
            String role = output.getAgentName();
            String model = output.getModelName() != null ? output.getModelName() : "";
            String avatar = "smart_toy";
            String color = "var(--primary)";
            String align = "left";

            switch (role) {
                case "STRATEGIST":
                    avatar = "search_insights";
                    color = "#4285F4"; // Google blue
                    break;
                case "CRITIC":
                    avatar = "gavel";
                    color = "#D97706"; // Anthropic amber
                    align = "right";
                    break;
                case "OPTIMIZER":
                    avatar = "lightbulb";
                    color = "#10A37F"; // OpenAI green
                    break;
                case "ARCHITECT":
                    avatar = "engineering";
                    color = "#7C3AED"; // Purple for Groq/Llama
                    align = "right";
                    break;
                case "SYNTHESIZER":
                    avatar = "summarize";
                    color = "#059669";
                    break;
                case "User (Moderator)":
                    avatar = "person";
                    color = "var(--slate-800)";
                    align = "right";
                    break;
                case "Coordinator":
                    avatar = "support_agent";
                    color = "#0EA5E9";
                    break;
            }

            String displayName = role;
            if (!model.isEmpty() && !"User (Moderator)".equals(role) && !"Coordinator".equals(role)) {
                displayName = role + " (" + model + ")";
            }

            String time = output.getGeneratedAt() != null
                    ? output.getGeneratedAt().substring(11,
                            Math.min(19, output.getGeneratedAt().length()))
                    : "";

            messages.add(AgentMessage.builder()
                    .sender(displayName)
                    .avatar(avatar)
                    .color(color)
                    .time(time)
                    .align(align)
                    .content(output.getOutput())
                    .modelName(model)
                    .build());
        }

        return DebateResponse.builder()
                .projectId(projectId)
                .status(session.getStatus())
                .message("Current debate status")
                .startedAt(session.getCreatedAt() != null ? session.getCreatedAt().toString() : "")
                .messages(messages)
                .build();
    }

    /**
     * Injects a user message into the ongoing debate.
     */
    public void injectMessage(UUID projectId, String userText) {
        List<ChatSession> sessions = chatSessionRepository.findByProjectId(projectId.toString()).collectList().block();
        ChatSession session = (sessions != null && !sessions.isEmpty()) ? sessions.get(sessions.size() - 1) : null;
        
        if (session == null) {
            throw new WarRoomException("SESSION_NOT_FOUND", "No active session found");
        }

        Project project = projectRepository.findById(projectId.toString())
                .orElseThrow(() -> new WarRoomException("PROJECT_NOT_FOUND", "Project not found"));

        // Save user message
        AgentOutput userOutput = AgentOutput.builder()
                .chatSessionId(session.getId())
                .projectId(project.getId())
                .agentName("User (Moderator)")
                .modelName("human")
                .output(userText)
                .iteration(99)
                .generatedAt(LocalDateTime.now().toString())
                .build();
        agentOutputRepository.save(userOutput);

        // Generate AI response to user intervention
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                List<AgentOutput> history = agentOutputRepository
                        .findByChatSessionIdOrderByGeneratedAtAsc(session.getId());
                String historyStr = history.stream()
                        .map(o -> o.getAgentName() + ": " + o.getOutput())
                        .collect(Collectors.joining("\n"));

                String prompt = "You are a Debate Coordinator. The human moderator just said: \""
                        + userText + "\".\n" +
                        "Based on the debate history:\n" + historyStr + "\n\n" +
                        "Respond briefly, acknowledging their point and explaining how the debate should incorporate their feedback. "
                        +
                        "Reference the uploaded document context where relevant. Keep it under 150 words.";

                String aiResponse = openAIClient.call(prompt);

                AgentOutput reaction = AgentOutput.builder()
                        .chatSessionId(session.getId())
                        .projectId(project.getId())
                        .agentName("Coordinator")
                        .modelName("gpt-4o-mini")
                        .output(aiResponse)
                        .iteration(100)
                        .generatedAt(LocalDateTime.now().toString())
                        .build();
                agentOutputRepository.save(reaction);
            } catch (Exception e) {
                log.error("Failed to process injection for project {}", projectId, e);
            }
        }, taskExecutor);
    }

    /**
     * Ends an active debate session and produces a final synthesis.
     */
    public void endDebate(UUID projectId) {
        List<ChatSession> sessions = chatSessionRepository.findByProjectId(projectId.toString()).collectList().block();
        ChatSession session = (sessions != null && !sessions.isEmpty()) ? sessions.get(sessions.size() - 1) : null;
        
        if (session == null) {
            throw new WarRoomException("SESSION_NOT_FOUND", "No active session found");
        }

        if ("COMPLETED".equals(session.getStatus())) {
            log.info("Session already completed for project: {}", projectId);
            return;
        }

        // Run final synthesis
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                List<AgentOutput> history = agentOutputRepository
                        .findByChatSessionIdOrderByGeneratedAtAsc(session.getId());
                String historyStr = history.stream()
                        .map(o -> o.getAgentName() + ": " + o.getOutput())
                        .collect(Collectors.joining("\n\n"));

                String synthesisPrompt = com.warroom.util.PromptTemplates.buildSynthesizerPrompt(historyStr);
                String synthesisResponse = openAIClient.call(synthesisPrompt);

                AgentOutput synthesis = AgentOutput.builder()
                        .chatSessionId(session.getId())
                        .projectId(session.getProjectId())
                        .agentName("SYNTHESIZER")
                        .modelName("gpt-4o-mini")
                        .output(synthesisResponse)
                        .iteration(999)
                        .generatedAt(LocalDateTime.now().toString())
                        .build();
                agentOutputRepository.save(synthesis);

                session.setStatus("COMPLETED");
                session.setEnded(true);
                session.setUpdatedAt(new Date());
                chatSessionRepository.save(session).block();

                log.info("Debate ended and synthesis completed for project: {}", projectId);
            } catch (Exception e) {
                log.error("Failed to end debate for project {}", projectId, e);
            }
        }, taskExecutor);
    }
}
