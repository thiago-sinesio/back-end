package com.chat.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.Map;

@Component
public class N8nClient {

    private final RestClient restClient;

    public N8nClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.n8n.webhook-url:http://localhost:5678/webhook/document-ingested}") String webhookUrl) {
        this.restClient = restClientBuilder
                .baseUrl(webhookUrl)
                .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void triggerIngestionWorkflow(String fileId, String fileName, int chunks) {
        try {
            restClient.post()
                    .body(Map.of(
                            "fileId", fileId,
                            "fileName", fileName,
                            "chunks", chunks,
                            "status", "SUCCESS"
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // Log error but don't crash
            System.err.println("Error calling n8n webhook: " + e.getMessage());
        }
    }
}
