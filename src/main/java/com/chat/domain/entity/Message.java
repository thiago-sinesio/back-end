package com.chat.domain.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Message — Entidade JPA de Mensagem
 * 
 * Responsabilidade:
 * - Representar uma mensagem individual no domínio
 * - Relacionar-se com uma Session
 * - Armazenar metadados de arquivo (opcional)
 * 
 * Princípios:
 * - Encapsulamento: Getters/Setters simples
 * - Imutabilidade: Timestamps são gerados automaticamente
 * - Relacionamento: Pertence a uma única Session
 */
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_session_id", columnList = "session_id"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class Message {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "session_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID sessionId;

    @Column(name = "role", nullable = false, length = 20)
    private String role;  // "USER" ou "ASSISTANT"

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "file_id", columnDefinition = "BINARY(16)")
    private UUID fileId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // ============================================================================
    // Construtores
    // ============================================================================

    public Message() {
    }

    public Message(UUID id, UUID sessionId, String role, String content, LocalDateTime timestamp) {
        this.id = id;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Message(UUID id, UUID sessionId, String role, String content, UUID fileId, LocalDateTime timestamp) {
        this.id = id;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.fileId = fileId;
        this.timestamp = timestamp;
    }

    // ============================================================================
    // Getters & Setters
    // ============================================================================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // ============================================================================
    // toString (para logging)
    // ============================================================================

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", sessionId=" + sessionId +
                ", role='" + role + '\'' +
                ", contentLength=" + (content != null ? content.length() : 0) +
                ", fileId=" + fileId +
                ", timestamp=" + timestamp +
                '}';
    }
}
