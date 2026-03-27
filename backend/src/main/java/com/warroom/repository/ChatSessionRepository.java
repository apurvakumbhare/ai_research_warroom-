package com.warroom.repository;

import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import com.warroom.entity.ChatSession;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;

@Repository
public interface ChatSessionRepository extends FirestoreReactiveRepository<ChatSession> {
    
    Flux<ChatSession> findByProjectId(String projectId);
    
    Flux<ChatSession> findByTopicId(String topicId);
}
