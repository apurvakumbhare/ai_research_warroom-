package com.warroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Enhanced DTO for returning project details, including audit data and status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private UUID id;
    private String title;
    private String description;
    private String type;
    private String status;
    private String finalContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
