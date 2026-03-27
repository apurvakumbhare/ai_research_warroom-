package com.warroom.repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import com.warroom.entity.DocumentEntity;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;

@Repository
public interface DocumentRepository extends FirestoreReactiveRepository<DocumentEntity> {
    
    /**
     * Retrieve documents by project ID.
     */
    Flux<DocumentEntity> findByProjectId(String projectId);
    
    /**
     * Retrieve documents by topic ID.
     */
    Flux<DocumentEntity> findByTopicId(String topicId);
}
