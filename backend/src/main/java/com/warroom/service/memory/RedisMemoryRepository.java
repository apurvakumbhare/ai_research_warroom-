package com.warroom.service.memory;

import com.warroom.exception.WarRoomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * Low-level repository for managing conversation memory stored in Redis.
 * Uses Redis LIST structure to maintain the order of messages in a session.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisMemoryRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "warroom:memory:";

    /**
     * Appends a new message to the end of the session list in Redis.
     * 
     * @param sessionId unique identifier for the debate session
     * @param message   the formatted role:json message
     */
    public void appendMessage(String sessionId, String message) {
        String key = buildKey(sessionId);
        try {
            log.debug("Appending message to Redis for session: {}", sessionId);
            redisTemplate.opsForList().rightPush(key, message);
        } catch (Exception e) {
            log.error("Failed to append message to Redis for session {}: {}", sessionId, e.getMessage());
            throw new WarRoomException("REDIS_STORAGE_ERROR", "Could not persist message to memory");
        }
    }

    /**
     * Retrieves all messages stored for a specific session.
     * 
     * @param sessionId unique identifier for the debate session
     * @return an immutable list of messages in chronological order
     */
    public List<String> getMessages(String sessionId) {
        String key = buildKey(sessionId);
        try {
            List<String> messages = redisTemplate.opsForList().range(key, 0, -1);
            return messages != null ? messages : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to retrieve messages from Redis for session {}: {}", sessionId, e.getMessage());
            throw new WarRoomException("REDIS_RETRIEVAL_ERROR", "Could not fetch session memory");
        }
    }

    /**
     * Deletes the conversation memory for a session from Redis.
     * 
     * @param sessionId unique identifier for the debate session
     */
    public void clearMemory(String sessionId) {
        String key = buildKey(sessionId);
        try {
            log.info("Clearing Redis memory for session: {}", sessionId);
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to clear Redis memory for session {}: {}", sessionId, e.getMessage());
            throw new WarRoomException("REDIS_DELETE_ERROR", "Could not clear session memory");
        }
    }

    private String buildKey(String sessionId) {
        return KEY_PREFIX + sessionId;
    }
}
