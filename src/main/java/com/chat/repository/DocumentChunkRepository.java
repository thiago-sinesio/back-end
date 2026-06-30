package com.chat.repository;

import com.chat.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    @Query(value = """
        SELECT id, file_id, session_id, content, chunk_index,
               embedding, token_count, created_at,
               1 - (embedding <=> :queryEmbedding\\:\\:vector) AS similarity
        FROM document_chunks
        WHERE session_id = :sessionId
        ORDER BY embedding <=> :queryEmbedding\\:\\:vector
        LIMIT :topK
        """, nativeQuery = true)
    List<Object[]> findSimilarBySessionId(
        @Param("queryEmbedding") float[] queryEmbedding,
        @Param("sessionId") UUID sessionId,
        @Param("topK") int topK
    );

    void deleteByFileId(UUID fileId);

    List<DocumentChunk> findBySessionIdOrderByChunkIndexAsc(UUID sessionId);
}
