package com.warroom.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.warroom.entity.DebateSession;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class DebateSessionRepository {

    private final Firestore firestore;

    public DebateSessionRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public DebateSession save(DebateSession session) {
        if (session.getId() == null) {
            session.setId(UUID.randomUUID().toString());
        }
        try {
            firestore.collection("debate_sessions").document(session.getId()).set(session).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save debate session", e);
        }
        return session;
    }

    public Optional<DebateSession> findById(String id) {
        try {
            var doc = firestore.collection("debate_sessions").document(id).get().get();
            if (doc.exists()) {
                return Optional.ofNullable(doc.toObject(DebateSession.class));
            }
        } catch (Exception e) {
        }
        return Optional.empty();
    }

    public Optional<DebateSession> findById(UUID id) {
        return findById(id.toString());
    }

    public Optional<DebateSession> findFirstByProjectIdOrderByStartedAtDesc(String projectId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection("debate_sessions")
                    .whereEqualTo("projectId", projectId)
                    .orderBy("startedAt", Query.Direction.DESCENDING)
                    .limit(1)
                    .get().get();
            if (!querySnapshot.isEmpty()) {
                return Optional.ofNullable(querySnapshot.getDocuments().get(0).toObject(DebateSession.class));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find session", e);
        }
        return Optional.empty();
    }

    public Optional<DebateSession> findFirstByProjectIdOrderByStartedAtDesc(UUID projectId) {
        return findFirstByProjectIdOrderByStartedAtDesc(projectId.toString());
    }
}
