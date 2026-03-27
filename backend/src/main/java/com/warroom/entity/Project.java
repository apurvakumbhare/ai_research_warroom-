package com.warroom.entity;

import com.google.cloud.spring.data.firestore.Document;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.*;
import java.util.Date;

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

    private String projectName;

    private String coreHypothesis;

    private String ownerId;

    private java.util.List<String> participantIds;

    private String type;

    private String status;

    private String finalContent;

    private Date createdAt;

    private Date updatedAt;
}
