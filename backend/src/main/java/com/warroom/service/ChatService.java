package com.warroom.service;

import com.warroom.dto.AgentMessage;
import com.warroom.dto.ChatSessionDto;
import com.warroom.entity.AgentOutput;
import com.warroom.entity.ChatSession;
import com.warroom.entity.Project;
import com.warroom.exception.WarRoomException;
import com.warroom.repository.AgentOutputRepository;
import com.warroom.repository.ChatSessionRepository;
import com.warroom.repository.ProjectRepository;
import com.warroom.service.agent.OpenAIClient;
import com.warroom.service.orchestrator.WarRoomOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ProjectRepository projectRepository;
    private final AgentOutputRepository agentOutputRepository;
    private final WarRoomOrchestrator warRoomOrchestrator;
    private final OpenAIClient openAIClient;
    private final com.google.cloud.firestore.Firestore firestore;

    public ChatSessionDto getStatus(String chatSessionId) {
        ChatSession session = chatSessionRepository.findById(chatSessionId).block();
        if (session == null) {
            List<ChatSession> sessions = chatSessionRepository.findByProjectId(chatSessionId).collectList().block();
            if (sessions != null && !sessions.isEmpty()) {
                session = sessions.get(sessions.size() - 1);
            }
        }
        if (session == null) {
            return ChatSessionDto.builder().status("NOT_STARTED").build();
        }
        return mapToDto(session, false);
    }

    public ChatSessionDto startChat(String chatSessionId) {
        ChatSession session = chatSessionRepository.findById(chatSessionId).block();
        if (session == null) {
            List<ChatSession> sessions = chatSessionRepository.findByProjectId(chatSessionId).collectList().block();
            if (sessions != null && !sessions.isEmpty()) {
                session = sessions.get(sessions.size() - 1);
            }
        }
        if (session != null && !"FAILED".equals(session.getStatus())) {
            return mapToDto(session, false); // Already started
        }
        
        // When frontend invokes this, chatSessionId is usually actually a Project ID for the FIRST session.
        Project project = projectRepository.findById(chatSessionId).orElseThrow(
                () -> new WarRoomException("PROJECT_NOT_FOUND", "Project not found: " + chatSessionId));
        
        session = ChatSession.builder()
                .projectId(project.getId())
                .title(project.getProjectName())
                .status("STARTED")
                .isEnded(false)
                .createdAt(new Date())
                .build();
        session = chatSessionRepository.save(session).block();
        
        warRoomOrchestrator.startOrchestration(UUID.fromString(project.getId()));
        return mapToDto(session, false);
    }

    public ChatSessionDto getChat(String chatSessionId) {
        // 1. Try direct session lookup
        ChatSession session = chatSessionRepository.findById(chatSessionId).block();
        
        // 2. Fallback: If not found, maybe it's a projectId - get latest session for that project
        if (session == null) {
            log.info("Session ID not found: {}. Checking if it's a Project ID.", chatSessionId);
            List<ChatSession> sessions = chatSessionRepository.findByProjectId(chatSessionId).collectList().block();
            if (sessions != null && !sessions.isEmpty()) {
                session = sessions.get(sessions.size() - 1); // Get latest conceptually
            }
        }

        if (session == null) {
            throw new WarRoomException("SESSION_NOT_FOUND", "Chat session not found for ID: " + chatSessionId);
        }
        
        return mapToDto(session, true);
    }

    public void endChat(String chatSessionId) {
        // Find session - try direct lookup first, then fallback by projectId
        ChatSession session = chatSessionRepository.findById(chatSessionId).block();
        if (session == null) {
            log.info("Session not found by ID: {}. Trying as projectId fallback.", chatSessionId);
            List<ChatSession> sessions = chatSessionRepository.findByProjectId(chatSessionId).collectList().block();
            if (sessions != null && !sessions.isEmpty()) {
                session = sessions.get(sessions.size() - 1);
            }
        }
        if (session == null) {
            throw new WarRoomException("SESSION_NOT_FOUND", "Chat not found: " + chatSessionId);
        }
        
        if ("COMPLETED".equals(session.getStatus())) {
            return;
        }

        try {
            List<AgentOutput> history = agentOutputRepository.findByChatSessionIdOrderByGeneratedAtAsc(session.getId());
            String historyStr = history.stream()
                    .map(o -> o.getAgentName() + ": " + o.getOutput())
                    .collect(Collectors.joining("\n\n"));

            String synthesisPrompt = com.warroom.util.PromptTemplates.buildSynthesizerPrompt(historyStr);
            String synthesisResponse = openAIClient.call(synthesisPrompt);

            AgentOutput synthesis = AgentOutput.builder()
                    .chatSessionId(session.getId())
                    .projectId(session.getProjectId())
                    .agentName("SYNTHESIZER")
                    .modelName("gpt-4o-mini")
                    .output(synthesisResponse)
                    .iteration(999)
                    .generatedAt(LocalDateTime.now().toString())
                    .build();
            agentOutputRepository.save(synthesis);

            session.setStatus("COMPLETED");
            session.setEnded(true);
            session.setUpdatedAt(new Date());
            
            // Native fallback URL since PDF generation works entirely client-side right now.
            session.setSummaryPdfUrl("native_ui_export_" + session.getId());
            chatSessionRepository.save(session).block();

        } catch (Exception e) {
            log.error("Failed to end chat for session {}", chatSessionId, e);
        }
    }

    public List<ChatSessionDto> getRecentChats() {
        // Retrieve all chat sessions conceptually
        List<ChatSession> all = chatSessionRepository.findAll().collectList().block();
        if (all == null) return new ArrayList<>();
        return all.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .map(s -> mapToDto(s, false))
                .collect(Collectors.toList());
    }

    public List<java.util.Map<String, Object>> getChatFiles(String chatSessionId) {
        ChatSession session = chatSessionRepository.findById(chatSessionId).block();
        String projectId = (session != null) ? session.getProjectId() : chatSessionId; 
        
        try {
             java.util.List<com.google.cloud.firestore.QueryDocumentSnapshot> docs = firestore
                    .collection("projects").document(projectId).collection("uploads").get().get().getDocuments();
            List<java.util.Map<String, Object>> uploads = new java.util.ArrayList<>();
            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : docs) {
                java.util.Map<String, Object> data = new java.util.HashMap<>(doc.getData());
                data.put("id", doc.getId());
                data.remove("extractedText"); // Don't bloat list view
                if (data.get("uploadedAt") instanceof com.google.cloud.Timestamp) {
                    com.google.cloud.Timestamp ts = (com.google.cloud.Timestamp) data.get("uploadedAt");
                    data.put("uploadedAt", ts.toDate().toString());
                }
                uploads.add(data);
            }
            return uploads;
        } catch (Exception e) {
            log.error("Failed to fetch uploads for chatSession/project: {}", projectId, e);
            return new java.util.ArrayList<>();
        }
    }

    private ChatSessionDto mapToDto(ChatSession session, boolean includeMessages) {
        Project project = projectRepository.findById(session.getProjectId()).orElse(null);
        String title = (session.getTitle() != null) ? session.getTitle() 
            : (project != null ? project.getProjectName() : "Unknown Project");

        List<AgentMessage> messages = new ArrayList<>();
        if (includeMessages) {
            List<AgentOutput> outputs = agentOutputRepository.findByChatSessionIdOrderByGeneratedAtAsc(session.getId());
            for (AgentOutput output : outputs) {
                // Formatting identical to prior responses
                String role = output.getAgentName();
                String avatar = "smart_toy";
                String color = "var(--primary)";
                String align = "left";

                if (role != null) {
                    switch (role) {
                        case "STRATEGIST": avatar = "search_insights"; color = "#4285F4"; break;
                        case "CRITIC": avatar = "gavel"; color = "#D97706"; align = "right"; break;
                        case "OPTIMIZER": avatar = "lightbulb"; color = "#10A37F"; break;
                        case "ARCHITECT": avatar = "engineering"; color = "#7C3AED"; align = "right"; break;
                        case "SYNTHESIZER": avatar = "summarize"; color = "#059669"; break;
                        case "User (Moderator)": avatar = "person"; color = "var(--slate-800)"; align = "right"; break;
                        case "Coordinator": avatar = "support_agent"; color = "#0EA5E9"; break;
                    }
                }
                String time = output.getGeneratedAt() != null && output.getGeneratedAt().length() > 19 
                    ? output.getGeneratedAt().substring(11, 19) : "";

                messages.add(AgentMessage.builder()
                        .sender(role)
                        .avatar(avatar)
                        .color(color)
                        .align(align)
                        .time(time)
                        .content(output.getOutput())
                        .modelName(output.getModelName())
                        .build());
            }
        }

        return ChatSessionDto.builder()
                .chatSessionId(session.getId())
                .projectId(session.getProjectId())
                .title(title)
                .status(session.getStatus())
                .isEnded(session.isEnded())
                .summaryPdfUrl(session.getSummaryPdfUrl())
                .messages(messages)
                .build();
    }
}
