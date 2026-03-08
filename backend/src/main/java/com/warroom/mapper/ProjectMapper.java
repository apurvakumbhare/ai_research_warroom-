package com.warroom.mapper;

import com.warroom.dto.ProjectRequest;
import com.warroom.dto.ProjectResponse;
import com.warroom.entity.Project;
import org.springframework.stereotype.Component;

/**
 * Stateless mapper for converting between Project entities and DTOs.
 * Leverages manual mapping to maintain full control over the translation
 * process.
 */
@Component
public class ProjectMapper {

    /**
     * Converts a ProjectRequest DTO into a new Project entity.
     * 
     * @param request the incoming creation request
     * @return a new Project entity with status initialized to CREATED
     */
    public Project toEntity(ProjectRequest request) {
        if (request == null) {
            return null;
        }

        return Project.builder()
                .projectName(request.getProjectName())
                .coreHypothesis(request.getCoreHypothesis())
                .type(request.getType())
                .status("CREATED")
                .build();
    }

    /**
     * Converts a Project entity into a ProjectResponse DTO.
     * 
     * @param project the JPA entity from the database
     * @return the structured response object
     */
    public ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectResponse.builder()
                .id(project.getId() != null ? java.util.UUID.fromString(project.getId()) : null)
                .projectName(project.getProjectName())
                .coreHypothesis(project.getCoreHypothesis())
                .ownerId(project.getOwnerId())
                .participantIds(project.getParticipantIds())
                .type(project.getType())
                .status(project.getStatus())
                .finalContent(project.getFinalContent())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing Project entity with data from a request.
     * Managed fields like id and createdAt are preserved.
     * 
     * @param request the update request
     * @param project the existing entity to be modified
     */
    public void updateEntity(ProjectRequest request, Project project) {
        if (request == null || project == null) {
            return;
        }

        project.setProjectName(request.getProjectName());
        project.setCoreHypothesis(request.getCoreHypothesis());
        project.setType(request.getType());
        // status, finalContent, and audit fields are handled by services or JPA
        // listeners
    }
}
