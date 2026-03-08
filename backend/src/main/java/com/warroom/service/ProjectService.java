package com.warroom.service;

import com.warroom.dto.ProjectRequest;
import com.warroom.dto.ProjectResponse;
import com.warroom.dto.WarRoomResult;
import com.warroom.entity.Project;
import com.warroom.exception.WarRoomException;
import com.warroom.mapper.ProjectMapper;
import com.warroom.repository.ProjectRepository;
import com.warroom.service.orchestrator.WarRoomOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing the business logic of projects.
 * Bridges the API layer with persistence and the AI orchestration engine.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final WarRoomOrchestrator warRoomOrchestrator;

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        log.info("Creating new project: {}", request.getProjectName());
        Project project = projectMapper.toEntity(request);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(UUID id) {
        log.debug("Retrieving project by ID: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new WarRoomException("PROJECT_NOT_FOUND", "Project not found with ID: " + id));
        return projectMapper.toResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAll() {
        log.debug("Retrieving all projects");
        return projectRepository.findAll().stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse update(UUID id, ProjectRequest request) {
        log.info("Updating project: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new WarRoomException("PROJECT_NOT_FOUND", "Project not found with ID: " + id));

        projectMapper.updateEntity(request, project);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponse(updatedProject);
    }

    @Transactional
    public void delete(UUID id) {
        log.warn("Deleting project: {}", id);
        if (!projectRepository.existsById(id)) {
            throw new WarRoomException("PROJECT_NOT_FOUND", "Cannot delete non-existent project: " + id);
        }
        projectRepository.deleteById(id);
    }

    /**
     * Triggers the multi-agent AI debate pipeline.
     */
    public WarRoomResult runWarRoom(UUID projectId) {
        log.info("Delegating to WarRoomOrchestrator for project: {}", projectId);
        return warRoomOrchestrator.executeWarRoom(projectId);
    }
}
