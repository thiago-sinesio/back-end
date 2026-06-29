package com.chat.service.rag;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentIngestionService {

    private final ParsingService parsingService;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final WebhookService webhookService;

    private final Map<String, IngestionResult> results = new ConcurrentHashMap<>();

    public DocumentIngestionService(
            ParsingService parsingService,
            ChunkingService chunkingService,
            EmbeddingService embeddingService,
            WebhookService webhookService) {
        this.parsingService = parsingService;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.webhookService = webhookService;
    }

    @Transactional
    public IngestionResult ingest(byte[] content, String originalName, String mimeType, long size) {
        String fileId = UUID.randomUUID().toString();

        String extractedText = parsingService.parse(content, mimeType);

        List<String> chunks = chunkingService.chunk(extractedText);

        for (String chunk : chunks) {
            float[] embedding = embeddingService.generateEmbedding(chunk);
        }

        try {
            webhookService.notify(fileId, originalName, chunks.size());
        } catch (Exception e) {
            webhookService.notifyError(fileId, originalName, e.getMessage());
        }

        IngestionResult result = new IngestionResult(fileId, originalName, extractedText.length(), chunks.size());
        results.put(fileId, result);
        return result;
    }

    public IngestionResult getStatus(String fileId) {
        IngestionResult result = results.get(fileId);
        if (result == null) {
            throw new IllegalArgumentException("Documento não encontrado: " + fileId);
        }
        return result;
    }

    public record IngestionResult(
            String fileId,
            String originalName,
            int totalCharacters,
            int totalChunks
    ) {}
}
