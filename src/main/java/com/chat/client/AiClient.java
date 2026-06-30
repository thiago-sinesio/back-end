package com.chat.client;

import com.chat.dto.EmbeddingRequest;
import com.chat.dto.EmbeddingResponse;
import com.chat.dto.LlmRequest;
import com.chat.dto.LlmResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Component
public class AiClient {

    private final RestClient restClient;
    
    @Value("${app.ai.model.chat:llama3.2}")
    private String chatModel;

    @Value("${app.ai.model.embedding:nomic-embed-text}")
    private String embeddingModel;

    public AiClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.ai.url:http://localhost:11434/v1}") String baseUrl,
            @Value("${app.ai.api-key:ollama}") String apiKey) {
        
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("HTTP-Referer", "http://localhost:8080")
                .defaultHeader("X-Title", "IAGEN RAG")
                .build();
    }

    public float[] generateEmbeddings(String text) {
        EmbeddingRequest request = new EmbeddingRequest(embeddingModel, text);
        
        EmbeddingResponse response = restClient.post()
                .uri("/embeddings")
                .body(request)
                .retrieve()
                .body(EmbeddingResponse.class);
                
        if (response != null && response.data() != null && !response.data().isEmpty()) {
            return response.data().get(0).embedding();
        }
        throw new RuntimeException("Failed to generate embeddings from AI provider");
    }
    
    public String generateChatCompletion(LlmRequest request) {
        // override model if not set
        if (request.model() == null) {
            request = new LlmRequest(chatModel, request.messages(), request.temperature(), request.maxTokens());
        }
        
        LlmResponse response = restClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(LlmResponse.class);
                
        if (response != null && response.choices() != null && !response.choices().isEmpty()) {
            return response.choices().get(0).message().content();
        }
        throw new RuntimeException("Failed to generate chat completion from AI provider");
    }
}
