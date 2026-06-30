package com.chat.controller;

import com.chat.dto.UploadResponse;
import com.chat.service.FileStorageService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final FileStorageService fileStorageService;
    private final DocumentIngestionService documentIngestionService;

    public UploadController(FileStorageService fileStorageService,
                            DocumentIngestionService documentIngestionService) {
        this.fileStorageService = fileStorageService;
        this.documentIngestionService = documentIngestionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sessionId") UUID sessionId) throws IOException {
        byte[] content = file.getBytes();
        String originalName = file.getOriginalFilename();
        String mimeType = file.getContentType();
        long size = file.getSize();

        UploadResponse response = fileStorageService.storeFile(content, originalName, mimeType, size, sessionId);

        documentIngestionService.ingest(response.fileId(), response.extractedText(), originalName, sessionId);

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<UploadResponse> getFileMetadata(@PathVariable UUID fileId) {
        UploadResponse response = fileStorageService.getFileMetadata(fileId);
        return ResponseEntity.ok(response);
    }
}
