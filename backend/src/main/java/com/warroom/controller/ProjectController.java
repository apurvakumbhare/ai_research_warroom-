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
    private final com.google.cloud.firestore.Firestore firestore;

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

    @GetMapping("/{id}/uploads")
    public ResponseEntity<List<java.util.Map<String, Object>>> getProjectUploads(@PathVariable UUID id) {
        log.debug("Fetching uploads for project: {}", id);
        try {
            java.util.List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = firestore
                    .collection("projects").document(id.toString()).collection("uploads").get().get().getDocuments();
            List<java.util.Map<String, Object>> uploads = new java.util.ArrayList<>();
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                java.util.Map<String, Object> data = new java.util.HashMap<>(doc.getData());
                data.put("id", doc.getId());
                data.remove("extractedText"); // Do not send full text to frontend table

                if (data.get("uploadedAt") instanceof com.google.cloud.Timestamp) {
                    com.google.cloud.Timestamp ts = (com.google.cloud.Timestamp) data.get("uploadedAt");
                    data.put("uploadedAt", ts.toDate().toString());
                }
                uploads.add(data);
            }
            return ResponseEntity.ok(uploads);
        } catch (Exception e) {
            log.error("Failed to fetch uploads for project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/uploads/{uploadId}")
    public ResponseEntity<java.util.Map<String, Object>> getProjectUpload(@PathVariable UUID id, @PathVariable String uploadId) {
        log.debug("Fetching upload details for project: {}, upload: {}", id, uploadId);
        try {
            com.google.cloud.firestore.DocumentSnapshot doc = firestore
                    .collection("projects").document(id.toString()).collection("uploads").document(uploadId).get().get();
            if (!doc.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            java.util.Map<String, Object> data = new java.util.HashMap<>(doc.getData());
            data.put("id", doc.getId());
            if (data.get("uploadedAt") instanceof com.google.cloud.Timestamp) {
                com.google.cloud.Timestamp ts = (com.google.cloud.Timestamp) data.get("uploadedAt");
                data.put("uploadedAt", ts.toDate().toString());
            }
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Failed to fetch upload {} for project: {}", uploadId, id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
