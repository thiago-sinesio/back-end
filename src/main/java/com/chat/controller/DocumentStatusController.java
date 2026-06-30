package com.chat.controller;

import com.chat.model.FileMetadata;
import com.chat.repository.DocumentChunkRepository;
import com.chat.repository.FileMetadataRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoint de status de documentos, utilizado pelo n8n
 * para verificar o andamento/conclusão da ingestão.
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentStatusController {

    private final FileMetadataRepository fileMetadataRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public DocumentStatusController(FileMetadataRepository fileMetadataRepository,
                                    DocumentChunkRepository documentChunkRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.documentChunkRepository = documentChunkRepository;
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> getDocumentStatus(@PathVariable UUID id) {
        return fileMetadataRepository.findById(id)
                .map(metadata -> {
                    long chunkCount = documentChunkRepository.findBySessionIdOrderByChunkIndexAsc(id).size();

                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("fileId", metadata.getId());
                    response.put("originalName", metadata.getOriginalName());
                    response.put("mimeType", metadata.getMimeType());
                    response.put("size", metadata.getSize());
                    response.put("status", chunkCount > 0 ? "INDEXED" : "PENDING");
                    response.put("chunks", chunkCount);
                    response.put("uploadedAt", metadata.getUploadedAt());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> notFound = new LinkedHashMap<>();
                    notFound.put("fileId", id);
                    notFound.put("status", "NOT_FOUND");
                    return ResponseEntity.status(404).body(notFound);
                });
    }
}
