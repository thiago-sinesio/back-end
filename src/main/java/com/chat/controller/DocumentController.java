package com.chat.controller;

import com.chat.dto.DocumentStatusResponse;
import com.chat.service.rag.DocumentIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;

    public DocumentController(DocumentIngestionService documentIngestionService) {
        this.documentIngestionService = documentIngestionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentStatusResponse> uploadDocument(
            @RequestParam("file") MultipartFile file) throws IOException {
        byte[] content = file.getBytes();
        String originalName = file.getOriginalFilename();
        String mimeType = file.getContentType();
        long size = file.getSize();

        DocumentIngestionService.IngestionResult result = documentIngestionService.ingest(
                content, originalName, mimeType, size);

        DocumentStatusResponse response = new DocumentStatusResponse(
                result.fileId(),
                result.originalName(),
                "completed",
                result.totalChunks(),
                result.totalCharacters()
        );
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<DocumentStatusResponse> getDocumentStatus(@PathVariable String id) {
        DocumentIngestionService.IngestionResult result = documentIngestionService.getStatus(id);
        DocumentStatusResponse response = new DocumentStatusResponse(
                result.fileId(),
                result.originalName(),
                "completed",
                result.totalChunks(),
                result.totalCharacters()
        );
        return ResponseEntity.ok(response);
    }
}
