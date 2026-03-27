package com.warroom.controller;

import com.warroom.dto.DebateResponse;
import com.warroom.dto.WarRoomResult;
import com.warroom.service.orchestrator.WarRoomOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for orchestrating the AI multi-agent debate process.
 */
@Slf4j
@RestController
@RequestMapping("/api/debate")
@RequiredArgsConstructor
public class DebateController {

    private final WarRoomOrchestrator warRoomOrchestrator;

    /**
     * Initiates the multi-agent debate pipeline for a specific project.
     */
    @PostMapping("/{projectId}")
    public ResponseEntity<WarRoomResult> startDebate(@PathVariable UUID projectId) {
        log.info("Starting AI debate for project id: {}", projectId);
        WarRoomResult result = warRoomOrchestrator.startOrchestration(projectId);
        return ResponseEntity.ok(result);
    }

    /**
     * Checks the current status of an ongoing debate.
     */
    @GetMapping("/{projectId}/status")
    public ResponseEntity<DebateResponse> getDebateStatus(@PathVariable UUID projectId) {
        log.debug("Checking debate status for project id: {}", projectId);
        DebateResponse response = warRoomOrchestrator.getOrchestrationStatus(projectId);
        return ResponseEntity.ok(response);
    }

    /**
     * Injects human intervention into the debate.
     */
    @PostMapping("/{projectId}/inject")
    public ResponseEntity<Void> injectMessage(@PathVariable UUID projectId,
            @RequestBody java.util.Map<String, String> payload) {
        log.info("Injecting human message into debate for project id: {}", projectId);
        String text = payload.get("text");
        warRoomOrchestrator.injectMessage(projectId, text);
        return ResponseEntity.ok().build();
    }

    /**
     * Ends an active debate session and triggers final synthesis.
     */
    @PostMapping("/{projectId}/end")
    public ResponseEntity<Void> endDebate(@PathVariable UUID projectId) {
        log.info("Ending debate for project id: {}", projectId);
        warRoomOrchestrator.endDebate(projectId);
        return ResponseEntity.ok().build();
    }
}
