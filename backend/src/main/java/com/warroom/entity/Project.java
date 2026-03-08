package com.warroom.entity;

import com.google.cloud.spring.data.firestore.Document;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a user-submitted research or startup project.
 * Serves as the root for all agent refinements and debate sessions.
 */
@Document(collectionName = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @DocumentId
    private String id;

    private String title;

    private String description;

    private String type;

    private String status;

    private String finalContent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
