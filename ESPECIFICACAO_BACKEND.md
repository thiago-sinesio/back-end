# ESPECIFICAÇÃO DO BACK-END — Spring Boot

## 1. Estrutura de Pastas

```
src/main/java/com/chat/
├── ChatApplication.java
├── config/
│   └── WebConfig.java                  # CORS, multipart config
├── controller/
│   ├── MessageController.java          # endpoints de mensagens
│   ├── UploadController.java           # endpoint de upload
│   └── HealthController.java           # health check
├── service/
│   ├── MessageService.java             # regras de negócio de mensagens
│   ├── SessionService.java             # gerenciamento de sessões
│   ├── FileStorageService.java         # processamento e armazenamento de arquivos
│   └── FileParserService.java          # extração de texto de PDF/TXT
├── repository/
│   ├── MessageRepository.java          # persistência de mensagens
│   └── SessionRepository.java          # persistência de sessões
├── model/
│   ├── Message.java                    # entidade JPA
│   ├── Session.java                    # entidade JPA
│   └── FileMetadata.java              # metadados do arquivo enviado
├── dto/
│   ├── MessageRequest.java             # payload de entrada
│   ├── MessageResponse.java            # payload de saída
│   ├── UploadResponse.java             # resposta do upload
│   └── HealthResponse.java             # resposta do health check
├── exception/
│   ├── GlobalExceptionHandler.java     # handler global @ControllerAdvice
│   └── SessionNotFoundException.java   # exceção customizada
└── mapper/
    └── MessageMapper.java              # conversão entre entidade e DTO

src/main/resources/
├── application.yml                     # configs (banco, upload limits, CORS)
└── static/                             # arquivos estáticos (se houver)
```

---

## 2. Modelos de Dados

### 2.1 Session

| Campo       | Tipo          | Descrição                            |
|-------------|---------------|--------------------------------------|
| id          | UUID (PK)     | Identificador único da sessão        |
| title       | String        | Nome opcional da sessão              |
| createdAt   | LocalDateTime | Timestamp de criação                 |
| updatedAt   | LocalDateTime | Timestamp da última atividade        |
| active      | Boolean       | Se a sessão está ativa              |

### 2.2 Message

| Campo       | Tipo          | Descrição                            |
|-------------|---------------|--------------------------------------|
| id          | UUID (PK)     | Identificador único da mensagem      |
| sessionId   | UUID (FK)     | Referência à sessão                  |
| role        | Enum          | `USER` ou `ASSISTANT`               |
| content     | TEXT          | Conteúdo da mensagem                 |
| fileId      | UUID (nullable) | Referência ao arquivo enviado      |
| timestamp   | LocalDateTime | Momento do envio                     |

### 2.3 FileMetadata

| Campo       | Tipo          | Descrição                            |
|-------------|---------------|--------------------------------------|
| id          | UUID (PK)     | Identificador único do arquivo       |
| originalName| String        | Nome original do arquivo             |
| storedName  | String        | Nome físico no disco                 |
| mimeType    | String        | `application/pdf` ou `text/plain`    |
| size        | Long          | Tamanho em bytes                     |
| extractedText| TEXT (nullable)| Texto extraído (PDF/TXT)           |
| uploadedAt  | LocalDateTime | Timestamp do upload                  |

---

## 3. Contratos da API REST

### 3.1 Health Check

```
GET /api/health
```

**Resposta (200):**
```json
{
  "status": "UP",
  "timestamp": "2026-06-25T10:30:00Z",
  "version": "1.0.0"
}
```

---

### 3.2 Mensagens

#### Enviar mensagem

```
POST /api/messages
```

**Request Body:**
```json
{
  "sessionId": "uuid-da-sessao",
  "role": "USER",
  "content": "Olá, qual a previsão do tempo?"
}
```

**Resposta (201):**
```json
{
  "id": "uuid-gerado",
  "sessionId": "uuid-da-sessao",
  "role": "USER",
  "content": "Olá, qual a previsão do tempo?",
  "fileId": null,
  "timestamp": "2026-06-25T10:30:00Z"
}
```

**Regras:**
- Se `sessionId` não existir, a service deve criar uma nova sessão automaticamente.
- O campo `role` é validado: apenas `USER` ou `ASSISTANT`.
- O campo `content` não pode ser vazio (a menos que `fileId` esteja presente).

#### Listar histórico de uma sessão

```
GET /api/messages/{sessionId}
```

**Parâmetros de query (opcionais):**
| Parâmetro | Tipo   | Padrão   | Descrição                     |
|-----------|--------|----------|-------------------------------|
| page      | int    | 0        | Número da página              |
| size      | int    | 50       | Tamanho da página             |
| sort      | String | timestamp | Ordenação (ex: `timestamp,asc`) |

**Resposta (200):**
```json
{
  "sessionId": "uuid-da-sessao",
  "messages": [
    {
      "id": "uuid",
      "sessionId": "uuid-da-sessao",
      "role": "USER",
      "content": "Olá",
      "fileId": null,
      "timestamp": "2026-06-25T10:30:00Z"
    },
    {
      "id": "uuid",
      "sessionId": "uuid-da-sessao",
      "role": "ASSISTANT",
      "content": "Olá! Como posso ajudar?",
      "fileId": null,
      "timestamp": "2026-06-25T10:30:05Z"
    }
  ],
  "page": 0,
  "size": 50,
  "totalElements": 2,
  "totalPages": 1
}
```

**Erro (404):**
```json
{
  "error": "SESSION_NOT_FOUND",
  "message": "Sessão não encontrada: uuid-invalido"
}
```

#### Criar nova sessão

```
POST /api/sessions
```

**Request Body (opcional):**
```json
{
  "title": "Conversa sobre clima"
}
```

**Resposta (201):**
```json
{
  "id": "uuid-gerado",
  "title": "Conversa sobre clima",
  "createdAt": "2026-06-25T10:30:00Z",
  "updatedAt": "2026-06-25T10:30:00Z",
  "active": true
}
```

#### Listar sessões

```
GET /api/sessions
```

**Resposta (200):**
```json
[
  {
    "id": "uuid-1",
    "title": "Conversa sobre clima",
    "createdAt": "2026-06-25T10:30:00Z",
    "updatedAt": "2026-06-25T10:35:00Z",
    "active": true
  },
  {
    "id": "uuid-2",
    "title": "Suporte técnico",
    "createdAt": "2026-06-24T09:00:00Z",
    "updatedAt": "2026-06-24T09:15:00Z",
    "active": false
  }
]
```

---

### 3.3 Upload de Arquivos

```
POST /api/upload
```

**Request:** `multipart/form-data`

| Campo   | Tipo           | Obrigatório | Descrição                              |
|---------|----------------|-------------|----------------------------------------|
| file    | MultipartFile  | Sim         | Arquivo PDF ou TXT (max 10MB)          |
| sessionId | UUID        | Sim         | Sessão à qual o arquivo pertence       |

**Resposta (201):**
```json
{
  "fileId": "uuid-gerado",
  "originalName": "relatorio.pdf",
  "mimeType": "application/pdf",
  "size": 204800,
  "extractedText": "conteúdo extraído do PDF...",
  "uploadedAt": "2026-06-25T10:30:00Z"
}
```

**Validações:**
- Tipo de arquivo permitido: `application/pdf` e `text/plain`.
- Tamanho máximo: 10 MB.
- Se a sessão não existir, retornar 404.

**Erro (400):**
```json
{
  "error": "INVALID_FILE_TYPE",
  "message": "Tipo de arquivo não suportado. Envie PDF ou TXT."
}
```

**Erro (413):**
```json
{
  "error": "FILE_TOO_LARGE",
  "message": "O arquivo excede o limite de 10MB."
}
```

---

### 3.4 Obter metadados do arquivo

```
GET /api/files/{fileId}
```

**Resposta (200):**
```json
{
  "fileId": "uuid",
  "originalName": "relatorio.pdf",
  "mimeType": "application/pdf",
  "size": 204800,
  "extractedText": "conteúdo extraído...",
  "uploadedAt": "2026-06-25T10:30:00Z"
}
```

---

## 4. Fluxo de Mensagens (com Resposta Mock do Assistente)

```
[Cliente] → POST /api/messages
               ↓
         [MessageController]
               ↓ (valida DTO)
         [MessageService]
               ↓ (regras de negócio)
         1. Verifica/recria sessão (SessionService)
         2. Salva mensagem do USER no banco (MessageRepository)
         3. Gera resposta mock do ASSISTANT (ver regras abaixo)
         4. Salva mensagem do ASSISTANT no banco (MessageRepository)
         5. Atualiza updatedAt da sessão (SessionRepository)
         6. Retorna AMBAS as mensagens (USER + ASSISTANT) na resposta
               ↓
         [MessageController]
               ↓ (HTTP 201)
         [Cliente]
```

**Regras de negócio (MessageService):**
- Ao receber uma mensagem com `role = USER`, a service **deve obrigatoriamente** gerar uma resposta automática do assistente (mock) na mesma requisição síncrona.
- **Comportamento mock (Etapa 1 — respostas controladas de teste):**
  - A resposta do assistente é gerada por um método `generateMockResponse(String userMessage)` dentro do `MessageService`.
  - O mock retorna respostas pré-definidas baseadas em palavras-chave simples. Exemplo:
    - Entrada contendo "olá" ou "oi" → `"Olá! Sou o assistente virtual. Como posso ajudar?"`
    - Entrada contendo "ajuda" → `"Posso te ajudar com informações gerais. Envie sua pergunta ou anexe um documento PDF/TXT para análise."`
    - Entrada contendo "arquivo" ou "documento" → `"Para enviar um documento, use a área de arrastar-e-soltar ou o botão de upload. Aceito arquivos PDF e TXT de até 10MB."`
    - Qualquer outra entrada → `"Recebi sua mensagem: '<trecho da mensagem>'. Em breve serei integrado a um modelo de IA para respostas mais inteligentes!"`
  - A lógica mock é isolada e facilmente substituível por uma integração real com modelo de IA em etapas futuras.
- O histórico completo de uma sessão é recuperado via `MessageRepository.findAllBySessionIdOrderByTimestampAsc()`.

**Formato da resposta `POST /api/messages` (atualizado):**
```json
{
  "userMessage": {
    "id": "uuid-user",
    "sessionId": "uuid-da-sessao",
    "role": "USER",
    "content": "Olá, tudo bem?",
    "fileId": null,
    "timestamp": "2026-06-25T10:30:00Z"
  },
  "assistantMessage": {
    "id": "uuid-assistant",
    "sessionId": "uuid-da-sessao",
    "role": "ASSISTANT",
    "content": "Olá! Sou o assistente virtual. Como posso ajudar?",
    "fileId": null,
    "timestamp": "2026-06-25T10:30:01Z"
  }
}
```

> **Nota para evolução futura:** Na Etapa 2, o método `generateMockResponse()` será substituído por uma chamada a um serviço de IA (ex: API do modelo). O contrato de resposta permanece o mesmo — o front-end não precisará ser alterado.

---

## 5. Fluxo de Upload de Arquivos

```
[Cliente] → POST /api/upload (multipart)
               ↓
         [UploadController]
               ↓ (valida extensão e tamanho)
         [FileStorageService]
               ↓
         1. Salva arquivo no disco (diretório configurável)
         2. Gera nome único (UUID + extensão original)
         3. Extrai texto (FileParserService)
               ↓
         [FileParserService]
               ↓
         1. Se PDF → extrai texto com PDFBox ou similar
         2. Se TXT → lê conteúdo como UTF-8
               ↓
         [FileStorageService]
               ↓
         4. Persiste FileMetadata (SessionRepository busca session)
         5. Cria mensagem automática com role=USER e fileId
         6. Retorna UploadResponse
               ↓
         [Cliente]
```

---

## 6. Responsabilidade de Cada Camada

### Controller
- Receber requisições HTTP.
- Validar parâmetros de entrada (via `@Valid`, DTOs e anotações).
- Chamar o service correspondente.
- Retornar resposta HTTP com status code adequado.
- **Não contém regras de negócio.**

### Service
- Conter todas as regras de negócio.
- Orquestrar chamadas a repositórios e outros serviços.
- Transformar entidades em DTOs (via mapper).
- Gerenciar transações (`@Transactional`).
- **Não lida com HTTP nem com parsing de request/resposta.**

### Repository
- Interface Spring Data JPA.
- Consultas ao banco de dados.
- **Não contém regras de negócio.**

### Config
- Beans de configuração (CORS, multipart, codificação).
- Filtros e interceptadores.

---

## 7. Regras de Persistência — Histórico por Sessão

- **Toda mensagem pertence a exatamente uma sessão** (`sessionId` FK).
- Ao buscar o histórico, retornar mensagens ordenadas por `timestamp ASC`.
- O backend jamais deve retornar mensagens de uma sessão diferente da solicitada.
- Sessões inativas (`active = false`) podem ter o histórico consultado, mas não podem receber novas mensagens (retornar 400).
- O banco sugerido é H2 (dev) ou PostgreSQL (prod), com JPA e Flyway para migrações.

---

## 8. Configuração (application.yml — template)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:chatdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

app:
  upload:
    dir: ${UPLOAD_DIR:./uploads}
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:5173}
```

---

## 9. Resumo dos Endpoints

| Método | Rota                  | Descrição                    |
|--------|------------------------|------------------------------|
| GET    | /api/health            | Health check do serviço      |
| POST   | /api/messages          | Enviar mensagem              |
| GET    | /api/messages/{sessionId} | Histórico da sessão       |
| POST   | /api/sessions          | Criar nova sessão            |
| GET    | /api/sessions          | Listar sessões               |
| POST   | /api/upload            | Upload de arquivo            |
| GET    | /api/files/{fileId}    | Metadados do arquivo         |

---

## 10. Tratamento de Erros (GlobalExceptionHandler)

A camada `exception/GlobalExceptionHandler` captura todas as exceções não tratadas e retorna respostas padronizadas:

```json
{
  "error": "ERROR_CODE",
  "message": "Descrição amigável do erro",
  "timestamp": "2026-06-25T10:30:00Z"
}
```

| Exceção                          | HTTP Status | Error Code              |
|----------------------------------|-------------|-------------------------|
| SessionNotFoundException         | 404         | SESSION_NOT_FOUND       |
| MethodArgumentNotValidException | 400         | VALIDATION_ERROR        |
| MaxUploadSizeExceededException   | 413         | FILE_TOO_LARGE          |
| InvalidFileTypeException         | 400         | INVALID_FILE_TYPE       |
| InactiveSessionException         | 400         | SESSION_INACTIVE        |
| Generic Exception                | 500         | INTERNAL_ERROR          |
