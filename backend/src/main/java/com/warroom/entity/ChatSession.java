package com.warroom.entity;

import com.google.cloud.spring.data.firestore.Document;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import java.util.Date;
import java.util.List;

/**
 * Entity representing an execution instance of the multi-agent debate/chat pipeline.
 */
@Document(collectionName = "chat_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    @DocumentId
    private String id;

    private String topicId; // Foreign key to Topic
    
    private String projectId; // Foreign key reference as String

    private String title;

    /**
     * Current status (STARTED, IN_PROGRESS, COMPLETED, FAILED, PAUSED)
     */
    private String status;

    private boolean isEnded; // crucial for frontend persistence loading

    private String summaryPdfUrl;

    private List<String> activeDocumentIds; // to inject via RAG

    private Date createdAt;

    private Date updatedAt;
}
