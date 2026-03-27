package com.warroom.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.warroom.entity.Project;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ProjectRepository {

    private final Firestore firestore;

    public ProjectRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Project save(Project project) {
        if (project.getId() == null) {
            project.setId(UUID.randomUUID().toString());
        }
        try {
            firestore.collection("projects").document(project.getId()).set(project).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save project", e);
        }
        return project;
    }

    public Optional<Project> findById(String id) {
        if (id == null) return Optional.empty();
        try {
            DocumentSnapshot doc = firestore.collection("projects").document(id).get().get();
            if (doc.exists()) {
                return Optional.ofNullable(doc.toObject(Project.class));
            }
        } catch (Exception e) {
            // ignore
        }
        return Optional.empty();
    }

    // Overload for backward compatibility with UUID
    public Optional<Project> findById(UUID id) {
        return findById(id.toString());
    }

    public List<Project> findAll() {
        try {
            return firestore.collection("projects").get().get().getDocuments().stream()
                    .map(doc -> doc.toObject(Project.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch projects", e);
        }
    }

    public boolean existsById(String id) {
        try {
            return firestore.collection("projects").document(id).get().get().exists();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean existsById(UUID id) {
        return existsById(id.toString());
    }

    public void deleteById(String id) {
        try {
            firestore.collection("projects").document(id).delete().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete project", e);
        }
    }

    public void deleteById(UUID id) {
        deleteById(id.toString());
    }
}
