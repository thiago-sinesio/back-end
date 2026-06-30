package com.chat.controller;

import com.chat.model.Session;
import com.chat.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<Session> createSession(@RequestBody(required = false) Map<String, String> body) {
        String title = body != null ? body.get("title") : null;
        Session session = sessionService.createSession(title);
        return ResponseEntity.status(201).body(session);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<Session>> listSessions() {
        List<Session> sessions = sessionService.listAll();
        return ResponseEntity.ok(sessions);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(@org.springframework.web.bind.annotation.PathVariable java.util.UUID id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
