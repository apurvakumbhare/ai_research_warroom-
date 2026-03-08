package com.warroom.service.orchestrator;

import com.warroom.dto.DebateResponse;
import com.warroom.dto.WarRoomResult;
import com.warroom.entity.AgentOutput;
import com.warroom.entity.DebateSession;
import com.warroom.entity.Project;
import com.warroom.exception.WarRoomException;
import com.warroom.repository.AgentOutputRepository;
import com.warroom.repository.DebateSessionRepository;
import com.warroom.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.util.UUID;

/**
 * Main orchestrator for the AI Research War-Room.
 * Manages the lifecycle of a debate session, including persistence and
 * final project updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarRoomOrchestrator {

    private final ProjectRepository projectRepository;
    private final DebateSessionRepository debateSessionRepository;
    private final AgentOutputRepository agentOutputRepository;
    private final DebatePipeline debatePipeline;
    private final com.warroom.service.agent.OpenAIClient openAIClient;

    /**
     * Executes the full multi-agent debate and refinement chain for a project.
     * 
     * @param projectId the project identifier
     * @return the final structured result of the orchestration
     */
    @Transactional
    public WarRoomResult executeWarRoom(UUID projectId) {
        log.info("Initiating War-Room orchestration for project: {}", projectId);

        // 1. Fetch Project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(
                        () -> new WarRoomException("PROJECT_NOT_FOUND", "Project not found with ID: " + projectId));

        // 2. Create and persist DebateSession
        DebateSession session = DebateSession.builder()
                .projectId(project.getId())
                .status("STARTED")
                .build();
        session = debateSessionRepository.save(session);
        log.debug("Created debate session: {}", session.getId());

        try {
            // 3. Update session status to IN_PROGRESS
            session.setStatus("IN_PROGRESS");
            debateSessionRepository.save(session);

            // 4. Run the AI Debate Pipeline
            WarRoomResult result = debatePipeline.runDebate(project, session);

            // 5. Store individual agent outputs (linked to project and session)
            // Note: In this implementation, AgentOutputs are assumed to be managed
            // and linked within the runDebate process or collected from it.
            // If the pipeline returns them, we save them here.

            // 6. Update Project with final refined content
            project.setFinalContent(result.getImprovedVersion());
            project.setStatus("COMPLETED");
            projectRepository.save(project);

            // 7. Finalize session
            session.setStatus("COMPLETED");
            session.setCompletedAt(result.getCompletedAt());
            debateSessionRepository.save(session);

            log.info("Orchestration completed successfully for project: {}", projectId);
            return result;

        } catch (Exception ex) {
            log.error("Orchestration failed for project {}: {}", projectId, ex.getMessage());
            session.setStatus("FAILED");
            debateSessionRepository.save(session);
            throw new WarRoomException("ORCHESTRATION_FAILED", "Failed to execute AI debate: " + ex.getMessage());
        }
    }

    /**
     * Entry point for starting the orchestration process.
     */
    public WarRoomResult startOrchestration(UUID projectId) {
        // Run asynchronously so the REST API can return immediately
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                executeWarRoom(projectId);
            } catch (Exception e) {
                log.error("Async execution failed for project {}", projectId, e);
            }
        });

        return WarRoomResult.builder().build(); // Initial empty return
    }

    /**
     * Retrieves the status of the latest orchestration for a project.
     */
    public DebateResponse getOrchestrationStatus(UUID projectId) {
        DebateSession session = debateSessionRepository.findFirstByProjectIdOrderByStartedAtDesc(projectId)
                .orElse(null);

        if (session == null) {
            return DebateResponse.builder()
                    .projectId(projectId)
                    .status("NOT_STARTED")
                    .message("No debate session found")
                    .build();
        }

        // Fetch outputs and map to AgentMessage
        java.util.List<com.warroom.dto.AgentMessage> messages = new java.util.ArrayList<>();
        java.util.List<AgentOutput> outputs = agentOutputRepository
                .findByDebateSessionIdOrderByGeneratedAtAsc(session.getId());

        for (AgentOutput output : outputs) {
            String role = output.getAgentName();
            String avatar = "smart_toy";
            String color = "var(--primary)";
            String align = "left";

            if ("RESEARCHER".equals(role)) {
                avatar = "search_insights";
                color = "var(--ocean)";
            } else if ("CRITIC".equals(role)) {
                avatar = "gavel";
                color = "var(--driftwood)";
                align = "right";
            } else if ("OPTIMIZER".equals(role) || "DEVIL".equals(role)) {
                avatar = "psychology";
                align = "right";
            }

            String time = output.getGeneratedAt() != null
                    ? String.format("%02d:%02d:%02d", output.getGeneratedAt().getHour(),
                            output.getGeneratedAt().getMinute(), output.getGeneratedAt().getSecond())
                    : "";

            messages.add(com.warroom.dto.AgentMessage.builder()
                    .sender(role)
                    .avatar(avatar)
                    .color(color)
                    .time(time)
                    .align(align)
                    .content(output.getOutput())
                    .build());
        }

        return DebateResponse.builder()
                .projectId(projectId)
                .status(session.getStatus())
                .message("Current debate status")
                .startedAt(session.getStartedAt())
                .messages(messages)
                .build();
    }

    /**
     * Injects a human intervention into the debate.
     */
    public void injectMessage(UUID projectId, String userText) {
        DebateSession session = debateSessionRepository.findFirstByProjectIdOrderByStartedAtDesc(projectId)
                .orElseThrow(() -> new WarRoomException("SESSION_NOT_FOUND", "No active session found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new WarRoomException("PROJECT_NOT_FOUND", "Project not found"));

        // 1. Save user input
        AgentOutput userOutput = AgentOutput.builder()
                .debateSessionId(session.getId())
                .projectId(project.getId())
                .agentName("User (Moderator)")
                .output(userText)
                .iteration(99)
                .generatedAt(java.time.LocalDateTime.now())
                .build();
        agentOutputRepository.save(userOutput);

        // 2. Fetch history and respond
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                java.util.List<AgentOutput> history = agentOutputRepository
                        .findByDebateSessionIdOrderByGeneratedAtAsc(session.getId());
                String historyStr = history.stream().map(o -> o.getAgentName() + ": " + o.getOutput())
                        .collect(java.util.stream.Collectors.joining("\n"));

                String prompt = "You are an AI Coordinator in the War-Room. The human moderator just intervened with: \""
                        + userText + "\".\n" +
                        "Based on the debate history:\n" + historyStr + "\n\n" +
                        "Respond briefly to the human intervention, proposing a way to incorporate their feedback into the active debate.";

                String aiResponse = openAIClient.call(prompt);

                AgentOutput reactionOutput = AgentOutput.builder()
                        .debateSessionId(session.getId())
                        .projectId(project.getId())
                        .agentName("Coordinator")
                        .output(aiResponse)
                        .iteration(100)
                        .generatedAt(java.time.LocalDateTime.now())
                        .build();
                agentOutputRepository.save(reactionOutput);
            } catch (Exception e) {
                log.error("Failed to process injection for project {}", projectId, e);
            }
        });
    }
}
