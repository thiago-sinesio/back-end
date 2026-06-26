package com.chat.application.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

/**
 * MessageRequest — DTO de entrada para enviar mensagem
 * 
 * Responsabilidade:
 * - Validações estruturais (não nulo, tamanho, formato)
 * - Mapear payload JSON de entrada para objeto Java
 * 
 * O que NÃO faz:
 * ✗ Validações de lógica de negócio (delegadas ao serviço)
 * ✗ Transformações complexas
 * ✗ Acesso a banco de dados
 */
public class MessageRequest {

    @NotNull(message = "sessionId não pode ser nulo")
    private UUID sessionId;

    @NotBlank(message = "role não pode ser vazio")
    @Size(min = 1, max = 20, message = "role deve ter entre 1 e 20 caracteres")
    private String role;

    @Size(max = 500, message = "content não pode exceder 500 caracteres")
    private String content;

    private UUID fileId;

    // ============================================================================
    // Construtores
    // ============================================================================

    public MessageRequest() {
    }

    public MessageRequest(UUID sessionId, String role, String content) {
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
    }

    public MessageRequest(UUID sessionId, String role, String content, UUID fileId) {
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.fileId = fileId;
    }

    // ============================================================================
    // Getters & Setters
    // ============================================================================

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

    // ============================================================================
    // toString (para logging)
    // ============================================================================

    @Override
    public String toString() {
        return "MessageRequest{" +
                "sessionId=" + sessionId +
                ", role='" + role + '\'' +
                ", contentLength=" + (content != null ? content.length() : 0) +
                ", fileId=" + fileId +
                '}';
    }
}
