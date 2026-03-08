package com.warroom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for incoming project creation requests.
 * Captures the core essence of the research or startup idea.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {

    @NotBlank(message = "Project Name is required")
    private String projectName;

    @NotBlank(message = "Core Hypothesis is required")
    private String coreHypothesis;

    private String type; // e.g., RESEARCH, STARTUP, ML
}
