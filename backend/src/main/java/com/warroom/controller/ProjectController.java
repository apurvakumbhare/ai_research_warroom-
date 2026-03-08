package com.warroom.controller;

import com.warroom.dto.ProjectRequest;
import com.warroom.dto.ProjectResponse;
import com.warroom.dto.WarRoomResult;
import com.warroom.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing AI Research Projects.
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest request) {
        log.info("Received request to create project: {}", request.getProjectName());
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id) {
        log.debug("Fetching project with id: {}", id);
        return ResponseEntity.ok(projectService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        log.debug("Fetching all projects");
        return ResponseEntity.ok(projectService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable UUID id, @RequestBody ProjectRequest request) {
        log.info("Updating project with id: {}", id);
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        log.warn("Deleting project with id: {}", id);
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Manually triggers the AI debate for a project.
     */
    @PostMapping("/{id}/run")
    public ResponseEntity<WarRoomResult> runWarRoom(@PathVariable UUID id) {
        log.info("Manually triggering AI debate for project: {}", id);
        return ResponseEntity.ok(projectService.runWarRoom(id));
    }
}
