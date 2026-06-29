package com.chat.service.rag;

import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

    public float[] generateEmbedding(String text) {
        return generateMockEmbedding(text);
    }

    private float[] generateMockEmbedding(String text) {
        int dimensions = 128;
        float[] embedding = new float[dimensions];
        if (text != null) {
            int hash = text.hashCode();
            for (int i = 0; i < dimensions; i++) {
                embedding[i] = (float) Math.sin(hash * (i + 1));
            }
        }
        return embedding;
    }
}
