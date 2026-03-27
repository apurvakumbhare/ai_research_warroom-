package com.warroom.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ChatSessionDto {
    private String chatSessionId;
    private String projectId;
    private String title;
    private String status;
    private boolean isEnded;
    private String summaryPdfUrl;
    private List<AgentMessage> messages;
}
