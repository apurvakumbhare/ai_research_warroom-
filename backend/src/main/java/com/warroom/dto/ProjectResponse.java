package com.warroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * DTO for returning project data to the frontend.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {

    private String id;

    private String projectName;

    private String coreHypothesis;

    private String ownerId;

    private List<String> participantIds;

    private String type;

    private String status;

    private String finalContent;

    private Date createdAt;

    private Date updatedAt;
}
