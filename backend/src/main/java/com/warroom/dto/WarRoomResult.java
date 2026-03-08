package com.warroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The final structured output of the AI Research War-Room.
 * Compiles the refined idea, risks, roadmap, and scoring.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarRoomResult {

    private UUID projectId;

    /**
     * The final, iteratively refined version of the user's idea.
     */
    private String improvedVersion;

    private List<String> keyImprovements;

    private List<String> risks;

    /**
     * Strategic roadmap organized by developmental phases (e.g., Phase 1, Phase 2).
     */
    private Map<String, List<String>> roadmap;

    private double confidenceScore;

    private LocalDateTime completedAt;
}
