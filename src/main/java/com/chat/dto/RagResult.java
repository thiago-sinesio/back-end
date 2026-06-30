package com.chat.dto;

import java.util.List;

/**
 * Resultado de uma consulta RAG contendo a resposta gerada pelo LLM
 * e as fontes (chunks) utilizadas como contexto.
 */
public record RagResult(
    String answer,
    List<Source> sources
) {
    /**
     * Representa um chunk recuperado pelo pipeline RAG.
     * Os campos estão alinhados com o contrato do frontend (models/message.js).
     */
    public record Source(
        String chunkId,
        String documentId,
        String documentName,
        int chunkIndex,
        double score,
        String content
    ) {}
}
