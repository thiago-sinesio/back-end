package com.chat.service.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WebhookService {

    private final String webhookUrl;
    private final RestTemplate restTemplate;

    public WebhookService(
            @Value("${app.rag.webhook.url:}") String webhookUrl,
            RestTemplate restTemplate) {
        this.webhookUrl = webhookUrl;
        this.restTemplate = restTemplate;
    }

    public void notify(String fileId, String originalName, int chunkCount) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        Map<String, Object> payload = Map.of(
                "event", "document.ingested",
                "fileId", fileId,
                "originalName", originalName,
                "chunkCount", chunkCount
        );
        restTemplate.postForEntity(webhookUrl, payload, String.class);
    }

    public void notifyError(String fileId, String originalName, String error) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        Map<String, Object> payload = Map.of(
                "event", "document.ingestion_error",
                "fileId", fileId,
                "originalName", originalName,
                "error", error
        );
        restTemplate.postForEntity(webhookUrl, payload, String.class);
    }
}
