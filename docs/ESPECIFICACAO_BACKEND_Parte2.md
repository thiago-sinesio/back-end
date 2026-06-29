# SYSTEM DOCS — Parte 2: RAG Pipeline, OpenRouter e n8n

**Versão:** 2.0  
**Pacote:** `com.chat` (expansão — contratos existentes inalterados)  
**Foco:** Pipeline de Ingestão, Retrieval-Augmented Generation (RAG), Integração OpenRouter, Orquestração n8n

---

## 1. Estrutura de Novas Pastas e Classes

```
src/main/java/com/chat/
│
├── client/                              # NOVO — Clientes HTTP externos
│   ├── OpenRouterClient.java           # Chamadas à API OpenRouter (LLM)
│   └── N8nClient.java                  # Chamadas aos webhooks do n8n
│
├── dto/
│   ├── ChatRequest.java                # NOVO — Payload para envio ao LLM
│   ├── ChatResponse.java               # NOVO — Resposta do LLM
│   ├── EmbeddingRequest.java           # NOVO — Payload de embedding
│   ├── EmbeddingResponse.java          # NOVO — Resposta de embedding
│   └── RagQueryResponse.java           # NOVO — Resposta do RAG com contexto
│
├── exception/
│   ├── LlmException.java               # NOVO — Erro de comunicação com LLM
│   └── EmbeddingException.java         # NOVO — Erro ao gerar embedding
│
├── model/
│   ├── DocumentChunk.java              # NOVO — Entidade de chunk (pgvector)
│   └── DocumentChunkId.java            # NOVO — ID composto (opcional)
│
├── repository/
│   ├── DocumentChunkRepository.java    # NOVO — Repositório pgvector
│   └── FileMetadataRepository.java     # NOVO — Repositório ausente na Parte 1
│
└── service/
    ├── ChunkingService.java            # NOVO — Divisão de texto em chunks
    ├── EmbeddingService.java           # NOVO — Geração de embeddings via OpenRouter
    ├── VectorSearchService.java        # NOVO — Busca por similaridade (pgvector)
    ├── RagService.java                 # NOVO — Orquestração RAG
    └── MessageService.java             # MODIFICADO — Substitui mock por RAG
```

### 1.1 Diagrama de Pacotes

```
com.chat
  ├── .client           (novo)  → OpenRouterClient, N8nClient
  ├── .config           (existe) → inalterado
  ├── .controller       (existe) → + RagController
  ├── .dto              (existe) → + ChatRequest/Response, EmbeddingRequest/Response, RagQueryResponse
  ├── .exception        (existe) → + LlmException, EmbeddingException
  ├── .model            (existe) → + DocumentChunk (+ índices pgvector)
  ├── .repository       (existe) → + DocumentChunkRepository, FileMetadataRepository
  └── .service          (existe) → + ChunkingService, EmbeddingService, VectorSearchService, RagService
                                  → MessageService (modificado internamente)
```

---

## 2. Novas Entidades e Relacionamentos

### 2.1 DocumentChunk

| Campo          | Tipo            | Descrição                                      |
|----------------|-----------------|------------------------------------------------|
| id             | UUID (PK)       | Identificador único do chunk                   |
| fileId         | UUID (FK)       | Referência ao FileMetadata                     |
| sessionId      | UUID (FK)       | Sessão associada ao upload                     |
| content        | TEXT            | Texto do chunk                                 |
| chunkIndex     | Integer         | Ordem do chunk no documento original           |
| embedding      | `vector(1536)`  | Vetor de embedding (pgvector)                  |
| tokenCount     | Integer         | Número aproximado de tokens                    |
| createdAt      | LocalDateTime   | Timestamp de criação                           |

**DDL:**
```sql
CREATE TABLE document_chunks (
    id          UUID PRIMARY KEY,
    file_id     UUID NOT NULL REFERENCES file_metadata(id),
    session_id  UUID NOT NULL REFERENCES sessions(id),
    content     TEXT NOT NULL,
    chunk_index INTEGER NOT NULL,
    embedding   VECTOR(1536),
    token_count INTEGER,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chunks_file_id ON document_chunks(file_id);
CREATE INDEX idx_chunks_session_id ON document_chunks(session_id);
CREATE INDEX idx_chunks_embedding ON document_chunks USING ivfflat (embedding vector_cosine_ops);
```

### 2.2 Relacionamentos

```
Session (1) ────< (N) Message
Session (1) ────< (N) DocumentChunk
FileMetadata (1) ────< (N) DocumentChunk
```

- Um `Session` pode ter múltiplos `DocumentChunk` (documentos processados na sessão).
- Um `FileMetadata` gera N `DocumentChunk`.
- `DocumentChunk.embedding` é o vetor `float8[]` do pgvector, armazenado como `VECTOR(1536)` (modelo `text-embedding-3-small` da OpenAI via OpenRouter).

---

## 3. Pipeline de Ingestão

```
[Upload Multipart]
       ↓
[UploadController] → delega para
       ↓
[FileStorageService] (existente)
       ↓  salva arquivo, persiste FileMetadata, extrai texto
       ↓
[ChunkingService] (novo)
       ↓  divide extractedText em chunks (~500 tokens)
       ↓
[EmbeddingService] (novo)
       ↓  envia cada chunk ao OpenRouter → recebe embedding vector
       ↓
[DocumentChunkRepository] (novo)
       ↓  persiste cada chunk + embedding no banco
       ↓
[N8nClient] (novo)
       ↓  notifica n8n via webhook (assíncrono via @Async)
       ↓
[Retorno 201 ao cliente]
```

### 3.1 Fluxo Detalhado

```
1. UploadController.receiveFile()
   ├── Valida: MultipartFile presente, sessionId válido
   ├── Chama: fileStorageService.storeFile(...)
   └── Retorna: 201 + UploadResponse

2. FileStorageService.storeFile()
   ├── Valida tipo e tamanho
   ├── Salva no disco
   ├── Extrai texto via FileParserService
   ├── Persiste FileMetadata via EntityManager
   ├── Cria Message automática (role=USER, "Arquivo enviado: ...")
   ├── Chama: chunkingService.chunkDocument(metadata.getId(), extractedText, sessionId)
   └── Retorna UploadResponse

3. ChunkingService.chunkDocument(fileId, text, sessionId)
   ├── Divide text em chunks de ~500 tokens (limite configurável)
   ├── Para cada chunk, chama: embeddingService.generateEmbedding(chunkContent)
   ├── Cria DocumentChunk com embedding e persiste via DocumentChunkRepository
   └── Retorna void (ou lista de IDs dos chunks)

4. EmbeddingService.generateEmbedding(text)
   ├── Monta EmbeddingRequest { model, input }
   ├── Chama: openRouterClient.createEmbedding(request)
   ├── Extrai float[] do EmbeddingResponse
   └── Retorna: float[] (vetor 1536 dimensões)

5. openRouterClient.createEmbedding(request)
   ├── POST https://openrouter.ai/api/v1/embeddings
   ├── Header: Authorization: Bearer ${OPENROUTER_API_KEY}
   ├── Header: Content-Type: application/json
   ├── Timeout: 30s
   └── Retorna: EmbeddingResponse

6. DocumentChunkRepository.saveAll(chunks)
   └── INSERT com embeddings no PostgreSQL (pgvector)

7. N8nClient.notifyIngestionComplete(fileId, chunkCount)
   ├── POST ${N8N_WEBHOOK_URL}/ingestion-complete
   └── Body: { fileId, chunkCount, sessionId, timestamp }
```

---

## 4. Serviços e Responsabilidades

### 4.1 ChunkingService

**Pacote:** `com.chat.service`

**Responsabilidades:**
- Receber texto extraído e dividir em chunks de tamanho configurável.
- Estratégia: divisão por parágrafos + recorte por contagem de tokens (500 tokens por chunk com overlap de 50 tokens).
- Não conhece banco de dados.
- Não conhece IA/embeddings.
- Retorna `List<String>` (texto de cada chunk).

```java
public class ChunkingService {
    public List<String> chunkText(String text, int maxTokens, int overlapTokens)
}
```

**Regras:**
- Preservar parágrafos (não quebrar no meio de uma linha).
- Se um parágrafo excede `maxTokens`, recortar no limite de tokens.
- Adicionar overlap de `overlapTokens` entre chunks consecutivos.
- Chunks com menos de 10 tokens após limpeza são descartados.

### 4.2 EmbeddingService

**Pacote:** `com.chat.service`

**Responsabilidades:**
- Receber uma string e gerar seu vetor de embedding via OpenRouter.
- Utilizar `OpenRouterClient` para chamada HTTP.
- Não conhece documentos nem chunks.
- Retorna `float[]` (vetor 1536 dimensões).
- Gerencia cache de embeddings (opcional).

```java
public class EmbeddingService {
    public float[] generateEmbedding(String text)
    public List<float[]> generateEmbeddings(List<String> texts)
}
```

### 4.3 VectorSearchService

**Pacote:** `com.chat.service`

**Responsabilidades:**
- Receber uma query embedding e buscar chunks similares no banco.
- Utilizar similaridade cosseno via pgvector (`<=>` operator).
- Não conhece LLM nem OpenRouter.
- Retornar `List<DocumentChunk>` ordenados por similaridade.

```java
public class VectorSearchService {
    public List<DocumentChunk> searchSimilar(float[] queryEmbedding, int topK, UUID sessionId)
}
```

### 4.4 RagService

**Pacote:** `com.chat.service`

**Responsabilidades:**
- Coordenar o fluxo completo RAG: receber pergunta do usuário → embedding → busca → montagem do prompt → chamada LLM → resposta.
- Montar o contexto com os chunks mais relevantes.
- Construir o prompt final (system + contexto + pergunta).
- Chamar `OpenRouterClient` para gerar resposta textual.
- Não conhece HTTP nem Controller.

```java
public class RagService {
    public RagQueryResponse answerQuery(String userMessage, UUID sessionId)
}
```

**Prompt template (RAG):**
```
Você é um assistente virtual especializado em responder perguntas com base nos
documentos fornecidos pelo usuário. Use APENAS o contexto abaixo para responder.
Se a resposta não estiver no contexto, diga que não encontrou a informação.

CONTEXTO:
{chunks_concatenados}

PERGUNTA DO USUÁRIO:
{userMessage}

RESPOSTA:
```

### 4.5 OpenRouterClient

**Pacote:** `com.chat.client`

**Responsabilidades:**
- Comunicação HTTP com a API do OpenRouter.
- Métodos para chat completion e embeddings.
- Gerencia headers de autenticação.
- Timeout e retry configuráveis.
- Não conhece domínio de negócio.

```java
public class OpenRouterClient {
    public ChatResponse sendChat(ChatRequest request)
    public EmbeddingResponse createEmbedding(EmbeddingRequest request)
}
```

### 4.6 N8nClient

**Pacote:** `com.chat.client`

**Responsabilidades:**
- Disparar webhooks para n8n em pontos específicos do pipeline.
- Chamadas assíncronas (`@Async`) para não bloquear o fluxo principal.
- Log de erros sem quebrar o fluxo principal.

```java
public class N8nClient {
    @Async
    public void notifyIngestionComplete(UUID fileId, int chunkCount, UUID sessionId)
}
```

### 4.7 MessageService (modificado)

**Pacote:** `com.chat.service`

**O que muda:**
- O método `sendMessage()` existente mantém o contrato público (mesmo DTO de entrada/saída).
- Internamente, `generateMockResponse()` é substituído por chamada a `RagService.answerQuery()`.
- Se `RagService` falhar ou retornar vazio, cai no mock como fallback.

```java
public Map<String, MessageResponse> sendMessage(MessageRequest request) {
    // ... validações existentes ...
    if (request.role() == USER) {
        String responseText = ragService.answerQuery(request.content(), sessionId);
        // Cria assistantMessage com responseText
    }
    // ... resto do fluxo inalterado ...
}
```

---

## 5. Repositories

### 5.1 DocumentChunkRepository

```java
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
               1 - (embedding <=> :queryEmbedding::vector) AS similarity
        FROM document_chunks
        WHERE session_id = :sessionId
        ORDER BY embedding <=> :queryEmbedding::vector
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
```

### 5.2 FileMetadataRepository

```java
package com.chat.repository;

import com.chat.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
}
```

> **Nota:** Atualmente `FileMetadata` é persistido via `EntityManager.persist()` no `FileStorageService`. O novo `FileMetadataRepository` resolve isso e evita acesso direto ao `EntityManager` no service.

---

## 6. DTOs

### 6.1 ChatRequest

```java
public record ChatRequest(
    String model,
    List<Message> messages,
    Double temperature,
    Integer maxTokens
) {
    public ChatRequest(String model, List<Message> messages) {
        this(model, messages, 0.7, 1024);
    }

    public record Message(String role, String content) {}
}
```

### 6.2 ChatResponse

```java
public record ChatResponse(
    String id,
    String model,
    List<Choice> choices,
    Usage usage
) {
    public record Choice(Message message, Integer index, String finishReason) {
        public record Message(String role, String content) {}
    }
    public record Usage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {}
}
```

### 6.3 EmbeddingRequest

```java
public record EmbeddingRequest(
    String model,
    String input
) {}
```

### 6.4 EmbeddingResponse

```java
public record EmbeddingResponse(
    String model,
    List<EmbeddingData> data,
    Usage usage
) {
    public record EmbeddingData(Integer index, float[] embedding, String object) {}
    public record Usage(Integer promptTokens, Integer totalTokens) {}
}
```

### 6.5 RagQueryResponse

```java
public record RagQueryResponse(
    String answer,
    List<ChunkContext> context,
    Integer totalTokensUsed
) {
    public record ChunkContext(
        UUID chunkId,
        String content,
        Double similarity,
        UUID fileId
    ) {}
}
```

---

## 7. Controllers e Endpoints REST

### 7.1 RagController (novo)

| Método | Rota                | Descrição                                      |
|--------|---------------------|------------------------------------------------|
| POST   | `/api/rag/query`    | Enviar pergunta com RAG                        |
| GET    | `/api/rag/status`   | Status da pipeline RAG (health check interno)  |

#### POST /api/rag/query

**Request Body:**
```json
{
  "sessionId": "uuid-da-sessao",
  "query": "Qual o prazo de entrega descrito no documento?",
  "topK": 5
}
```

**Response (200):**
```json
{
  "answer": "O prazo de entrega descrito no documento é de 30 dias úteis...",
  "context": [
    {
      "chunkId": "uuid-chunk",
      "content": "O prazo de entrega...",
      "similarity": 0.92,
      "fileId": "uuid-file"
    }
  ],
  "totalTokensUsed": 450
}
```

#### GET /api/rag/status

**Response (200):**
```json
{
  "status": "UP",
  "openRouterConnected": true,
  "pgvectorAvailable": true,
  "n8nWebhookConfigured": true,
  "totalChunks": 1523,
  "totalFilesIndexed": 12
}
```

### 7.2 Endpoints Modificados

#### POST /api/messages (modificado)

O contrato de **entrada e saída permanece idêntico** ao da Parte 1. Apenas a implementação interna do `MessageService` muda (mock → RAG + LLM).

**Request (inalterado):**
```json
{
  "sessionId": "uuid-da-sessao",
  "role": "USER",
  "content": "Qual o prazo de entrega?"
}
```

**Response (201) (inalterado):**
```json
{
  "userMessage": { ... },
  "assistantMessage": {
    "content": "O prazo de entrega descrito no documento é de 30 dias úteis..."
  }
}
```

---

## 8. Fluxo Completo do RAG

### 8.1 Fluxo de Pergunta (Query)

```
[Cliente] → POST /api/messages { content: "Qual o prazo?" }
                ↓
         [MessageController] → valida DTO
                ↓
         [MessageService.sendMessage()] (modificado)
                ↓
         1. Verifica sessão ativa
         2. Salva mensagem do USER
         3. Chama: ragService.answerQuery(content, sessionId)
                ↓
         [RagService.answerQuery()]
                ↓
         4. Gera embedding da pergunta via EmbeddingService
                ↓
         5. Busca chunks similares via VectorSearchService (topK=5)
                ↓
         6. Concatena chunks como contexto
                ↓
         7. Monta prompt RAG (system + contexto + pergunta)
                ↓
         8. Chama OpenRouterClient.sendChat() com prompt completo
                ↓
         9. Extrai resposta do ChatResponse
                ↓
         10. Retorna RagQueryResponse { answer, context, tokens }
                ↓
         [MessageService]
                ↓
         11. Cria Message role=ASSISTANT com answer
         12. Salva no banco
         13. Atualiza updatedAt da sessão
                ↓
         [MessageController]
                ↓
         [Cliente] ← 201 { userMessage, assistantMessage }
```

### 8.2 Fluxo de Ingestão (Upload → Indexação)

```
[Cliente] → POST /api/upload (multipart + sessionId)
                ↓
         [UploadController]
                ↓
         [FileStorageService.storeFile()]
                ↓
         1. Salva arquivo no disco
         2. Extrai texto (FileParserService)
         3. Persiste FileMetadata
         4. Cria Message automática
         5. Chunk: chunkingService.chunkDocument(fileId, text, sessionId)
                ↓
         [ChunkingService.chunkDocument()]
                ↓
         6. Divide texto em List<String> chunks
                ↓
         [EmbeddingService.generateEmbeddings()]
                ↓
         7. Para cada chunk: POST OpenRouter /embeddings
                ↓
         [DocumentChunkRepository.saveAll()]
                ↓
         8. Persiste DocumentChunk (content + embedding + metadados)
                ↓
         [N8nClient.notifyIngestionComplete()] (assíncrono)
                ↓
         [UploadController]
                ↓
         [Cliente] ← 201 { UploadResponse }
```

---

## 9. Integração com OpenRouter

### 9.1 API Keys e Configuração

```yaml
app:
  openrouter:
    api-key: ${OPENROUTER_API_KEY}
    base-url: https://openrouter.ai/api/v1
    chat-model: openai/gpt-4o-mini
    embedding-model: openai/text-embedding-3-small
    timeout: 30000
    max-retries: 2
```

### 9.2 OpenRouter Endpoints

| Funcionalidade | Endpoint                                      | Método |
|----------------|-----------------------------------------------|--------|
| Chat Completion | `https://openrouter.ai/api/v1/chat/completions` | POST   |
| Embedding       | `https://openrouter.ai/api/v1/embeddings`      | POST   |

### 9.3 Chat Completion Request

```json
{
  "model": "openai/gpt-4o-mini",
  "messages": [
    {
      "role": "system",
      "content": "Você é um assistente virtual..."
    },
    {
      "role": "user",
      "content": "[CONTEXTO]\n...\n\n[PERGUNTA]\nQual o prazo?"
    }
  ],
  "temperature": 0.3,
  "max_tokens": 1024
}
```

### 9.4 Embedding Request

```json
{
  "model": "openai/text-embedding-3-small",
  "input": "O prazo de entrega é de 30 dias úteis..."
}
```

### 9.5 Tratamento de Erros

- Se OpenRouter retornar HTTP 429 (rate limit): aguardar e retentar (até 2x).
- Se OpenRouter retornar HTTP 5xx: logar erro, lançar `LlmException`.
- Se embedding falhar: chunk não é persistido, logado como erro, pipeline continua com próximos chunks.
- Se chat completion falhar: `MessageService` usa resposta mock como fallback.

---

## 10. Integração com n8n

### 10.1 Webhooks

| Evento | Webhook | Método | Body |
|--------|---------|--------|------|
| Ingestão concluída | `${N8N_BASE_URL}/webhook/ingestion-complete` | POST | `{ fileId, chunkCount, sessionId, timestamp }` |
| Erro na ingestão | `${N8N_BASE_URL}/webhook/ingestion-error` | POST | `{ fileId, error, sessionId, timestamp }` |
| Nova pergunta (opcional) | `${N8N_BASE_URL}/webhook/query-log` | POST | `{ sessionId, query, tokenCount, responseTime }` |

### 10.2 Configuração

```yaml
app:
  n8n:
    base-url: ${N8N_BASE_URL:http://localhost:5678}
    webhook-ingestion-complete: /webhook/ingestion-complete
    webhook-ingestion-error: /webhook/ingestion-error
    webhook-query-log: /webhook/query-log
    enabled: true
```

### 10.3 Orquestração via n8n

O n8n pode orquestrar workflows como:
1. **Pós-ingestão:** Ao receber `ingestion-complete`, disparar workflow de sumarização, notificação por email, ou atualização de dashboard.
2. **Monitoria:** Logar queries para análise de uso e qualidade das respostas.
3. **Manutenção:** Workflow periódico para re-embedding de chunks (quando modelo mudar).

---

## 11. Tratamento de Erros

### 11.1 Novas Exceções

| Exceção              | HTTP Status | Error Code           | Causa                                  |
|----------------------|-------------|----------------------|----------------------------------------|
| LlmException         | 502         | LLM_SERVICE_ERROR    | OpenRouter indisponível ou timeout     |
| EmbeddingException   | 502         | EMBEDDING_ERROR      | Falha ao gerar embedding               |
| ChunkingException    | 500         | CHUNKING_ERROR       | Erro interno ao dividir texto          |

### 11.2 GlobalExceptionHandler (modificado)

Adicionar handlers para as novas exceções:

```java
@ExceptionHandler(LlmException.class)
public ResponseEntity<Map<String, Object>> handleLlmError(LlmException ex) {
    return buildErrorResponse(HttpStatus.BAD_GATEWAY, "LLM_SERVICE_ERROR", ex.getMessage());
}

@ExceptionHandler(EmbeddingException.class)
public ResponseEntity<Map<String, Object>> handleEmbeddingError(EmbeddingException ex) {
    return buildErrorResponse(HttpStatus.BAD_GATEWAY, "EMBEDDING_ERROR", ex.getMessage());
}
```

### 11.3 Estratégia de Fallback

- **EmbeddingService falha:** Chunk não é indexado, erro logado, pipeline continua com próximo chunk.
- **VectorSearch retorna vazio:** `RagService` envia pergunta ao LLM sem contexto adicional.
- **OpenRouter chat falha:** `MessageService` usa `generateMockResponse()` como fallback (comportamento da Parte 1).
- **n8n webhook falha:** Apenas log de erro, pipeline principal não é afetado (chamada assíncrona).

---

## 12. Responsabilidade de Cada Camada

### 12.1 Controller
- Validar parâmetros HTTP (`@Valid`, `@RequestParam`, `@PathVariable`).
- Chamar service correspondente.
- Retornar HTTP status code adequado.
- **Sem regra de negócio.**

### 12.2 Service
- **ChunkingService:** Apenas divide texto. Não conhece banco nem IA.
- **EmbeddingService:** Apenas gera embedding via OpenRouter. Não conhece documentos.
- **VectorSearchService:** Apenas busca no banco. Não conhece LLM.
- **RagService:** Apenas coordena fluxo (embedding → busca → prompt → LLM).
- **MessageService:** Orquestra a resposta do assistente.
- **Sem lógica de banco direta** (delega para Repository).
- **Sem HTTP.**

### 12.3 Client
- **OpenRouterClient:** Apenas comunicação HTTP com OpenRouter. Não conhece domínio.
- **N8nClient:** Apenas disparar webhooks para n8n.

### 12.4 Repository
- **DocumentChunkRepository:** Apenas queries SQL/JPA.
- **FileMetadataRepository:** Apenas CRUD básico.
- **Sem regra de negócio.**

### 12.5 Model
- **DocumentChunk:** Apenas mapeamento JPA com campo `embedding` para pgvector.

---

## 13. Diagramas Textuais dos Principais Fluxos

### 13.1 Pipeline de Ingestão

```
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│  Client  │   │ Upload   │   │FileStore │   │ Chunking │   │Embedding │   │   DB     │
│          │   │Controller│   │ Service  │   │ Service  │   │ Service  │   │pgvector  │
└────┬─────┘   └────┬─────┘   └────┬─────┘   └────┬─────┘   └────┬─────┘   └────┬─────┘
     │ POST /upload │              │              │              │              │
     ├──────────────►│              │              │              │              │
     │  multipart    │              │              │              │              │
     │               │ storeFile()  │              │              │              │
     │               ├──────────────►│              │              │              │
     │               │              │ parseText()   │              │              │
     │               │              ├──────────────►│              │              │
     │               │              │   extractedText              │              │
     │               │              │◄──────────────┤              │              │
     │               │              │              │              │              │
     │               │              │ chunkDoc()   │              │              │
     │               │              ├──────────────►│              │              │
     │               │              │              │ chunk texts   │              │
     │               │              │◄──────────────┤              │              │
     │               │              │              │              │              │
     │               │              │ for each chunk               │              │
     │               │              │──────────────►──────────────►│              │
     │               │              │              │              │ POST /embedd │
     │               │              │              │              ├──────────────►│
     │               │              │              │              │   OpenRouter  │
     │               │              │              │              │◄──────────────┤
     │               │              │              │              │ embedding vec │
     │               │              │              ├──────────────┘              │
     │               │              │              │ saveAll(chunks+embeddings)   │
     │               │              │              ├─────────────────────────────►│
     │               │              │              │                              │
     │               │              │ notify() async                              │
     │               │              ├──────────────► N8nClient                    │
     │               │              │                                              │
     │               │◄──────────────┤                                              │
     │               │  UploadResponse                                            │
     │◄──────────────┤              │                                              │
     │   201 + JSON  │              │                                              │
┌────┴─────┐   ┌────┴─────┐   ┌────┴─────┐   ┌────┴─────┐   ┌────┴─────┐   ┌────┴─────┐
│  Client  │   │ Upload   │   │FileStore │   │ Chunking │   │Embedding │   │   DB     │
│          │   │Controller│   │ Service  │   │ Service  │   │ Service  │   │pgvector  │
└──────────┘   └──────────┘   └──────────┘   └──────────┘   └──────────┘   └──────────┘
```

### 13.2 Fluxo de Pergunta com RAG

```
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│  Client  │  │ Message  │  │ Message  │  │   RAG    │  │Embedding │  │ Vector   │  │OpenRouter│
│          │  │Controller│  │ Service  │  │ Service  │  │ Service  │  │  Search  │  │  Client  │
└────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘
     │ POST /msg   │             │             │             │             │             │
     ├─────────────►│             │             │             │             │             │
     │              │ sendMessage │             │             │             │             │
     │              ├────────────►│             │             │             │             │
     │              │             │ answerQuery │             │             │             │
     │              │             ├────────────►│             │             │             │
     │              │             │             │ generateEmb │             │             │
     │              │             │             ├────────────►│             │             │
     │              │             │             │             │ POST /emb   │             │
     │              │             │             │             ├─────────────►             │
     │              │             │             │             │  embedding  │             │
     │              │             │             │             │◄────────────┤             │
     │              │             │             │◄────────────┤             │             │
     │              │             │             │             │             │             │
     │              │             │             │ searchSim() │             │             │
     │              │             │             ├──────────────────────────►│             │
     │              │             │             │  similar chunks           │             │
     │              │             │             │◄──────────────────────────┤             │
     │              │             │             │             │             │             │
     │              │             │             │ sendChat()  │             │             │
     │              │             │             ├────────────────────────────────────────►│
     │              │             │             │  prompt+sistema+contexto                 │
     │              │             │             │◄────────────────────────────────────────┤
     │              │             │             │  response text                           │
     │              │             │◄────────────┤             │             │             │
     │              │             │             │             │             │             │
     │              │             │ save assistant msg       │             │             │
     │              │◄────────────┤             │             │             │             │
     │◄─────────────┤             │             │             │             │             │
     │ 201 + JSON   │             │             │             │             │             │
┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐
│  Client  │  │ Message  │  │ Message  │  │   RAG    │  │Embedding │  │ Vector   │  │OpenRouter│
│          │  │Controller│  │ Service  │  │ Service  │  │ Service  │  │  Search  │  │  Client  │
└──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘
```

---

## 14. Regras Arquiteturais (Parte 2)

### 14.1 Regras Obrigatórias

| Regra | Descrição |
|-------|-----------|
| R1 | Controller não possui regra de negócio — apenas delega para Service |
| R2 | Service apenas orquestra — não contém lógica de negócio complexa |
| R3 | Repository apenas acessa o banco — sem regras de negócio |
| R4 | EmbeddingService não conhece documentos — recebe `String`, retorna `float[]` |
| R5 | ChunkingService não conhece banco — recebe `String`, retorna `List<String>` |
| R6 | Parser não conhece IA — `FileParserService` existente não é alterado |
| R7 | RagService apenas coordena fluxo — chama serviços na ordem correta |
| R8 | Nenhuma regra de negócio pode ficar em Controller ou Repository |
| R9 | Não modificar contratos existentes da Parte 1 — DTOs de entrada/saída de mensagens e upload permanecem idênticos |

### 14.2 Regras de Dependência

```
Controller → Service → Client
                    → Repository
Service → Service (apenas orquestração, sem regra de negócio)
Client → HTTP externo (OpenRouter, n8n)
Repository → Spring Data JPA → PostgreSQL/pgvector
```

### 14.3 Regras de Acoplamento

- `MessageService` conhece `RagService` (injeção).
- `RagService` conhece `EmbeddingService`, `VectorSearchService`, `OpenRouterClient`.
- `EmbeddingService` conhece `OpenRouterClient`.
- `ChunkingService` não conhece nenhum outro serviço.
- `VectorSearchService` conhece `DocumentChunkRepository`.
- `N8nClient` não conhece nenhum serviço de negócio.
- Nenhum Service conhece Controller ou DTOs de apresentação.

### 14.4 Regras de Transação

- `MessageService.sendMessage()`: `@Transactional` (já existe).
- `FileStorageService.storeFile()`: `@Transactional` (já existe, estendido para incluir persistência de chunks).
- `EmbeddingService.generateEmbedding()`: sem transação (chamada HTTP externa).
- `DocumentChunkRepository.saveAll()`: transação interna do JPA.

### 14.5 Regras de Assincronicidade

- Webhooks para n8n são disparados com `@Async` — não bloqueiam a response do upload.
- Chamadas ao OpenRouter são síncronas (bloqueantes) — o RAG precisa da resposta do LLM para completar o fluxo.

---

## Apêndice A — Configurações Adicionais (application.yml)

```yaml
app:
  # ... configs existentes ...

  openrouter:
    api-key: ${OPENROUTER_API_KEY}
    base-url: https://openrouter.ai/api/v1
    chat-model: openai/gpt-4o-mini
    embedding-model: openai/text-embedding-3-small
    timeout: 30000
    max-retries: 2
    temperature: 0.3
    max-tokens: 1024

  rag:
    chunk:
      max-tokens: 500
      overlap-tokens: 50
      min-chunk-tokens: 10
    search:
      top-k: 5
      similarity-threshold: 0.7

  n8n:
    base-url: ${N8N_BASE_URL:http://localhost:5678}
    webhook:
      ingestion-complete: /webhook/ingestion-complete
      ingestion-error: /webhook/ingestion-error
      query-log: /webhook/query-log
    enabled: true

spring:
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
```

## Apêndice B — Dependências Maven (novas)

```xml
<!-- Apache PDFBox (já existe) -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.3</version>
</dependency>
```

Nenhuma dependência nova adicional é necessária. O pgvector é uma extensão do PostgreSQL, não uma dependência Java — a interação é via SQL nativo (`@Query nativeQuery = true`) com o operador `<=>`.

> **Nota:** Se o perfil `dev` com H2 for mantido, os testes do RAG devem usar um perfil `rag-test` com PostgreSQL + pgvector, ou usar mocks para `VectorSearchService` e `EmbeddingService`.

---

**Fim da Especificação Técnica — Parte 2**  
**Próximo passo:** Geração de código Java (Entity → Repository → Service → Controller → Client) respeitando esta especificação.
