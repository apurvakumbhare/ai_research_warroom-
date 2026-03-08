package com.warroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents the output from an individual AI agent in the refinement chain.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentResponse {

    private String agentName;

    private String output;

    private int iteration;

    private LocalDateTime generatedAt;
}
