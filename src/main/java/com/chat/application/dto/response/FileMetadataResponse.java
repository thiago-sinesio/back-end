package com.chat.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * FileMetadataResponse — DTO de saída para metadados de arquivo
 * 
 * Responsabilidade:
 * - Serializar metadados de arquivo para JSON
 * - Remover campos sensíveis (ex: caminho completo no disco)
 */
public class FileMetadataResponse {

    @JsonProperty("fileId")
    private String fileId;

    private String originalName;

    private String mimeType;

    private Long size;

    private String extractedText;

    private String uploadedAt;  // ISO-8601: yyyy-MM-dd'T'HH:mm:ss'Z'

    // ============================================================================
    // Construtores
    // ============================================================================

    public FileMetadataResponse() {
    }

    public FileMetadataResponse(String fileId, String originalName, String mimeType, 
                                Long size, String uploadedAt) {
        this.fileId = fileId;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }

    public FileMetadataResponse(String fileId, String originalName, String mimeType, 
                                Long size, String extractedText, String uploadedAt) {
        this.fileId = fileId;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.size = size;
        this.extractedText = extractedText;
        this.uploadedAt = uploadedAt;
    }

    // ============================================================================
    // Getters & Setters
    // ============================================================================

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
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

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    // ============================================================================
    // toString (para logging)
    // ============================================================================

    @Override
    public String toString() {
        return "FileMetadataResponse{" +
                "fileId='" + fileId + '\'' +
                ", originalName='" + originalName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", size=" + size +
                ", extractedTextLength=" + (extractedText != null ? extractedText.length() : 0) +
                ", uploadedAt='" + uploadedAt + '\'' +
                '}';
    }
}
