package com.warroom.controller;

import com.warroom.dto.ChatSessionDto;
import com.warroom.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{id}/status")
    public ResponseEntity<ChatSessionDto> getChatStatus(@PathVariable String id) {
        return ResponseEntity.ok(chatService.getStatus(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ChatSessionDto> startChat(@PathVariable String id) {
        return ResponseEntity.ok(chatService.startChat(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatSessionDto> getChat(@PathVariable String id) {
        return ResponseEntity.ok(chatService.getChat(id));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<Void> endChat(@PathVariable String id) {
        chatService.endChat(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ChatSessionDto>> getRecentChats() {
        return ResponseEntity.ok(chatService.getRecentChats());
    }

    @GetMapping("/{id}/files")
    public ResponseEntity<List<java.util.Map<String, Object>>> getChatFiles(@PathVariable String id) {
        return ResponseEntity.ok(chatService.getChatFiles(id));
    }
}
