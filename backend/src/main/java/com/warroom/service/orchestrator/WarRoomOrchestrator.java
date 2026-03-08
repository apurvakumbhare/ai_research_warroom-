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
        return executeWarRoom(projectId);
    }

    /**
     * Retrieves the status of the latest orchestration for a project.
     */
    public DebateResponse getOrchestrationStatus(UUID projectId) {
        DebateSession session = debateSessionRepository.findFirstByProjectIdOrderByStartedAtDesc(projectId)
                .orElseThrow(
                        () -> new WarRoomException("SESSION_NOT_FOUND", "No session found for project: " + projectId));

        return DebateResponse.builder()
                .projectId(projectId)
                .status(session.getStatus())
                .message("Current debate status")
                .startedAt(session.getStartedAt())
                .build();
    }
}
