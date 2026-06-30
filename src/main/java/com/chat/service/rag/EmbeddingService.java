package com.chat.service.rag;

import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

    private final com.chat.client.AiClient aiClient;

    public EmbeddingService(com.chat.client.AiClient aiClient) {
        this.aiClient = aiClient;
    }

    public float[] generateEmbedding(String text) {
        return aiClient.generateEmbeddings(text);
    }
}
