package com.warroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response object for debate status and initial triggers.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DebateResponse {

    private UUID projectId;

    /**
     * Current status of the debate process (e.g., STARTED, IN_PROGRESS, COMPLETED).
     */
    private String status;

    private String message;

    private LocalDateTime startedAt;
}
