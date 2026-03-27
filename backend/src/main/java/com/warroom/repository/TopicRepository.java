package com.warroom.repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import com.warroom.entity.Topic;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends FirestoreReactiveRepository<Topic> {
    
    // We can define custom query methods here later, e.g., finding topics by projectId
    // Flux<Topic> findByProjectId(String projectId);
}
