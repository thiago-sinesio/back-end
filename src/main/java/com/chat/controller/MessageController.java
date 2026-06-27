package com.chat.controller;

import com.chat.dto.MessageRequest;
import com.chat.dto.MessageResponse;
import com.chat.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/messages")
    public ResponseEntity<Map<String, MessageResponse>> sendMessage(
            @Valid @RequestBody MessageRequest request) {
        Map<String, MessageResponse> response = messageService.sendMessage(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<Map<String, Object>> getHistory(
            @PathVariable UUID sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "timestamp,asc") String sort) {
        Map<String, Object> history = messageService.getHistory(sessionId, page, size, sort);
        return ResponseEntity.ok(history);
    }
}
