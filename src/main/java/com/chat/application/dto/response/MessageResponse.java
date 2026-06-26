package com.chat.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * MessageResponse — DTO de saída para mensagem
 * 
 * Responsabilidade:
 * - Serializar dados de mensagem para JSON
 * - Garantir formato consistente de resposta
 * 
 * O que NÃO faz:
 * ✗ Lógica de negócio
 * ✗ Validações
 * ✗ Transformações complexas
 */
public class MessageResponse {

    private UUID id;

    @JsonProperty("sessionId")
    private UUID sessionId;

    private String role;

    private String content;

    @JsonProperty("fileId")
    private UUID fileId;

    private String timestamp;  // ISO-8601: yyyy-MM-dd'T'HH:mm:ss'Z'

    // ============================================================================
    // Construtores
    // ============================================================================

    public MessageResponse() {
    }

    public MessageResponse(UUID id, UUID sessionId, String role, String content, String timestamp) {
        this.id = id;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    public MessageResponse(UUID id, UUID sessionId, String role, String content, UUID fileId, String timestamp) {
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // ============================================================================
    // toString (para logging)
    // ============================================================================

    @Override
    public String toString() {
        return "MessageResponse{" +
                "id=" + id +
                ", sessionId=" + sessionId +
                ", role='" + role + '\'' +
                ", contentLength=" + (content != null ? content.length() : 0) +
                ", fileId=" + fileId +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
