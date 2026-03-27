package com.warroom.entity;

import com.google.cloud.spring.data.firestore.Document;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import java.util.Date;

/**
 * Entity representing a file uploaded for context.
 */
@Document(collectionName = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentEntity { // Named DocumentEntity because com.google.cloud.spring.data.firestore.Document is an annotation

    @DocumentId
    private String id;

    private String projectId;

    private String topicId; // Optional, if bound to a specific topic

    private String fileName;

    private String fileUrl;

    private String extractedText;

    private Date uploadedAt;
}
