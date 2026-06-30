package com.chat.service.rag;

import com.chat.client.N8nClient;
import com.chat.model.DocumentChunk;
import com.chat.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentIngestionService {

    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final N8nClient n8nClient;
    private final DocumentChunkRepository documentChunkRepository;

    public DocumentIngestionService(
            ChunkingService chunkingService,
            EmbeddingService embeddingService,
            N8nClient n8nClient,
            DocumentChunkRepository documentChunkRepository) {
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.n8nClient = n8nClient;
        this.documentChunkRepository = documentChunkRepository;
    }

    @Transactional
    public void ingest(UUID fileId, String extractedText, String originalName, UUID sessionId) {
        if (extractedText == null || extractedText.isBlank()) {
            return;
        }

        List<String> chunks = chunkingService.chunk(extractedText);
        List<DocumentChunk> documentChunks = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            float[] embedding = embeddingService.generateEmbedding(chunk);
            
            documentChunks.add(new DocumentChunk(
                    fileId,
                    sessionId,
                    chunk,
                    i,
                    embedding,
                    chunk.length()
            ));
        }
        
        if (!documentChunks.isEmpty()) {
            documentChunkRepository.saveAll(documentChunks);
        }

        try {
            n8nClient.triggerIngestionWorkflow(fileId.toString(), originalName, chunks.size());
        } catch (Exception e) {
            System.err.println("Webhook N8n failed: " + e.getMessage());
        }
    }
}
