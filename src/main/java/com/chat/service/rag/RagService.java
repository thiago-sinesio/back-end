package com.chat.service.rag;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    public String query(String userQuery) {
        List<String> relevantChunks = retrieveContext(userQuery);
        return buildResponse(userQuery, relevantChunks);
    }

    private List<String> retrieveContext(String query) {
        return List.of();
    }

    private String buildResponse(String userQuery, List<String> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "Não encontrei informações relevantes no conhecimento atual para responder: " + userQuery;
        }
        String context = String.join("\n", chunks);
        return "Com base no conhecimento disponível:\n" + context;
    }
}
