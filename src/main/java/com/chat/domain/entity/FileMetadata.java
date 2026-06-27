package com.chat.domain.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FileMetadata — Entidade JPA de Metadados de Arquivo
 * 
 * Responsabilidade:
 * - Armazenar informações sobre arquivos enviados
 * - Rastrear nome original, tipo MIME e tamanho
 * - Manter texto extraído de PDFs/TXTs
 * 
 * Princípios:
 * - Rastreabilidade: Campos de auditoria (uploadedAt)
 * - Imutabilidade: Campos não mudam após criação
 */
@Entity
@Table(name = "file_metadata", indexes = {
        @Index(name = "idx_uploaded_at", columnList = "uploaded_at")
})
public class FileMetadata {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(name = "mime_type", nullable = false, length = 50)
    private String mimeType;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    // ============================================================================
    // Construtores
    // ============================================================================

    public FileMetadata() {
    }

    public FileMetadata(UUID id, String originalName, String storedName, 
                       String mimeType, Long size, LocalDateTime uploadedAt) {
        this.id = id;
        this.originalName = originalName;
        this.storedName = storedName;
        this.mimeType = mimeType;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }

    public FileMetadata(UUID id, String originalName, String storedName, 
                       String mimeType, Long size, String extractedText, LocalDateTime uploadedAt) {
        this.id = id;
        this.originalName = originalName;
        this.storedName = storedName;
        this.mimeType = mimeType;
        this.size = size;
        this.extractedText = extractedText;
        this.uploadedAt = uploadedAt;
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

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoredName() {
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    // ============================================================================
    // toString (para logging)
    // ============================================================================

    @Override
    public String toString() {
        return "FileMetadata{" +
                "id=" + id +
                ", originalName='" + originalName + '\'' +
                ", storedName='" + storedName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", size=" + size +
                ", extractedTextLength=" + (extractedText != null ? extractedText.length() : 0) +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
