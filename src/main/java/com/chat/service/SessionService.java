package com.chat.service;

import com.chat.model.Session;
import com.chat.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chat.repository.MessageRepository;
import com.chat.repository.DocumentChunkRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public SessionService(SessionRepository sessionRepository, 
                          MessageRepository messageRepository, 
                          DocumentChunkRepository documentChunkRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.documentChunkRepository = documentChunkRepository;
    }

    @Transactional
    public Session createSession(String title) {
        Session session = new Session(title);
        return sessionRepository.save(session);
    }

    public Session findById(UUID id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new com.chat.exception.SessionNotFoundException(id));
    }

    public Session findActiveSession(UUID id) {
        Session session = findById(id);
        if (!Boolean.TRUE.equals(session.getActive())) {
            throw new com.chat.exception.InactiveSessionException(id);
        }
        return session;
    }

    public List<Session> listAll() {
        return sessionRepository.findAllByOrderByUpdatedAtDesc();
    }

    @Transactional
    public void updateActivity(UUID sessionId) {
        Session session = findById(sessionId);
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    @Transactional
    public void deleteSession(UUID id) {
        Session session = findById(id);
        messageRepository.deleteBySessionId(id);
        documentChunkRepository.deleteBySessionId(id);
        sessionRepository.delete(session);
    }
}
