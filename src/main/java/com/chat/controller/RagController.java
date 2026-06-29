package com.chat.controller;

import com.chat.dto.ChatRequest;
import com.chat.dto.ChatResponse;
import com.chat.service.rag.RagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String answer = ragService.query(request.question());
        ChatResponse response = new ChatResponse(request.question(), answer);
        return ResponseEntity.ok(response);
    }
}
