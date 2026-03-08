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

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Project type is required")
    private String type; // e.g., RESEARCH, STARTUP, ML
}
