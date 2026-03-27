package com.warroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing an individual message in a debate timeline.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentMessage {
    private String sender;
    private String avatar;
    private String color;
    private String time;
    private String content;
    private String align;
    private String modelName;
}
