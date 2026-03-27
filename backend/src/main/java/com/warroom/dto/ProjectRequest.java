package com.warroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating or updating a project.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRequest {

    private String projectName;

    private String coreHypothesis;

    private String type;

    private List<String> participantIds;
}
