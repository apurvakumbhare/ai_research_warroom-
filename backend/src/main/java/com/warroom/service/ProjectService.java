package com.warroom.service;

import com.warroom.dto.ProjectRequest;
import com.warroom.dto.ProjectResponse;
import com.warroom.dto.WarRoomResult;
import com.warroom.entity.Project;
import com.warroom.exception.WarRoomException;
import com.warroom.repository.ProjectRepository;
import com.warroom.service.orchestrator.WarRoomOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for managing AI Research Projects.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WarRoomOrchestrator warRoomOrchestrator;

    public ProjectResponse create(ProjectRequest request) {
        log.info("Creating project: {}", request.getProjectName());
        Project project = Project.builder()
                .projectName(request.getProjectName())
                .coreHypothesis(request.getCoreHypothesis())
                .type(request.getType())
                .participantIds(request.getParticipantIds())
                .status("CREATED")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        project = projectRepository.save(project);
        return toResponse(project);
    }

    public ProjectResponse getById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new WarRoomException("PROJECT_NOT_FOUND",
                        "Project not found with ID: " + id));
        return toResponse(project);
    }

    public List<ProjectResponse> getAll() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new WarRoomException("PROJECT_NOT_FOUND",
                        "Project not found with ID: " + id));

        if (request.getProjectName() != null) project.setProjectName(request.getProjectName());
        if (request.getCoreHypothesis() != null) project.setCoreHypothesis(request.getCoreHypothesis());
        if (request.getType() != null) project.setType(request.getType());
        if (request.getParticipantIds() != null) project.setParticipantIds(request.getParticipantIds());
        project.setUpdatedAt(new Date());

        project = projectRepository.save(project);
        return toResponse(project);
    }

    public void delete(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new WarRoomException("PROJECT_NOT_FOUND", "Project not found with ID: " + id);
        }
        projectRepository.deleteById(id);
    }

    public WarRoomResult runWarRoom(UUID projectId) {
        log.info("Running War-Room for project: {}", projectId);
        return warRoomOrchestrator.startOrchestration(projectId);
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
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
}
