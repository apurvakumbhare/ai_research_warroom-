package com.warroom.entity;

import com.google.cloud.spring.data.firestore.Document;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing an execution instance of the multi-agent debate pipeline.
 */
@Document(collectionName = "debate_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebateSession {

    @DocumentId
    private String id;

    /**
     * Current status (STARTED, IN_PROGRESS, COMPLETED, FAILED)
     */
    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private String projectId; // Foreign key reference as String
}
