package com.warroom.repository;

import com.google.cloud.firestore.Firestore;
import com.warroom.entity.AgentOutput;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AgentOutputRepository {

    private final Firestore firestore;

    public AgentOutputRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public AgentOutput save(AgentOutput output) {
        if (output.getId() == null) {
            output.setId(UUID.randomUUID().toString());
        }
        try {
            firestore.collection("agent_outputs").document(output.getId()).set(output).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save agent output", e);
        }
        return output;
    }

    public List<AgentOutput> findByDebateSessionId(String sessionId) {
        try {
            return firestore.collection("agent_outputs")
                    .whereEqualTo("debateSessionId", sessionId)
                    .get().get().getDocuments().stream()
                    .map(doc -> doc.toObject(AgentOutput.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching outputs", e);
        }
    }

    public List<AgentOutput> findByDebateSessionId(UUID sessionId) {
        return findByDebateSessionId(sessionId.toString());
    }

    public List<AgentOutput> findByDebateSessionIdOrderByGeneratedAtAsc(String sessionId) {
        List<AgentOutput> outputs = findByDebateSessionId(sessionId);
        outputs.sort((a, b) -> {
            if (a.getGeneratedAt() == null || b.getGeneratedAt() == null)
                return 0;
            return a.getGeneratedAt().compareTo(b.getGeneratedAt());
        });
        return outputs;
    }
}
