package com.warroom.entity;

import com.google.cloud.spring.data.firestore.Document;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import java.util.Date;

/**
 * Entity representing a sub-domain inside a Project that holds unique ChatSessions.
 */
@Document(collectionName = "topics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {

    @DocumentId
    private String id;

    private String projectId; // Foreign key to Project

    private String title;

    private Date createdAt;

    private Date updatedAt;
}
