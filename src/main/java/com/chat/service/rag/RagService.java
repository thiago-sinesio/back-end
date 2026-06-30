package com.chat.service.rag;

import com.chat.client.AiClient;
import com.chat.dto.LlmRequest;
import com.chat.dto.RagResult;
import com.chat.model.FileMetadata;
import com.chat.repository.DocumentChunkRepository;
import com.chat.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RagService {

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final AiClient aiClient;

    public RagService(
            EmbeddingService embeddingService,
            DocumentChunkRepository documentChunkRepository,
            FileMetadataRepository fileMetadataRepository,
            AiClient aiClient) {
        this.embeddingService = embeddingService;
        this.documentChunkRepository = documentChunkRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.aiClient = aiClient;
    }

    /**
     * Executa o pipeline RAG completo:
     * 1. Vetoriza a pergunta do usuário
     * 2. Busca chunks similares no banco (pgvector)
     * 3. Monta o prompt com contexto
     * 4. Envia ao LLM e devolve resposta + fontes
     */
    public RagResult query(String userQuery, UUID sessionId) {
        // 1. Gerar embedding da pergunta
        float[] queryEmbedding = embeddingService.generateEmbedding(userQuery);

        // 2. Buscar chunks similares
        // Query retorna: id[0], file_id[1], session_id[2], content[3], chunk_index[4],
        //                embedding[5], token_count[6], created_at[7], similarity[8]
        List<Object[]> similarChunks = documentChunkRepository.findSimilarBySessionId(queryEmbedding, sessionId, 5);

        // 3. Extrair textos e montar sources alinhadas ao contrato do frontend
        List<String> contextTexts = new ArrayList<>();
        List<RagResult.Source> sources = new ArrayList<>();

        for (Object[] row : similarChunks) {
            UUID chunkId = (UUID) row[0];
            UUID fileId = (UUID) row[1];
            String content = (String) row[3];
            int chunkIndex = ((Number) row[4]).intValue();
            double similarity = ((Number) row[8]).doubleValue();

            // Filtro de qualidade: ignorar chunks com baixa relevância
            if (similarity < 0.3) continue;

            // Buscar nome do documento original
            String documentName = fileMetadataRepository.findById(fileId)
                    .map(FileMetadata::getOriginalName)
                    .orElse("Documento");

            contextTexts.add(content);
            sources.add(new RagResult.Source(
                    chunkId.toString(),
                    fileId.toString(),
                    documentName,
                    chunkIndex,
                    similarity,
                    content
            ));
        }

        // 4. Construir prompt e chamar LLM
        String answer = callLlm(userQuery, contextTexts);

        return new RagResult(answer, sources);
    }

    private String callLlm(String userQuery, List<String> chunks) {
        String prompt;
        if (chunks.isEmpty()) {
            prompt = "Responda a pergunta do usuário de forma natural (não cite contexto inexistente): " + userQuery;
        } else {
            String context = String.join("\n---\n", chunks);
            prompt = "Responda a pergunta do usuário com base no contexto abaixo.\n\nContexto:\n" + context + "\n\nPergunta: " + userQuery;
        }

        LlmRequest request = new LlmRequest(null, List.of(
                new LlmRequest.Message("system", "Você é um assistente prestativo. Use o contexto fornecido para basear sua resposta, mas responda naturalmente em Português do Brasil."),
                new LlmRequest.Message("user", prompt)
        ));

        return aiClient.generateChatCompletion(request);
    }
}
