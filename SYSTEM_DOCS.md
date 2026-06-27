# SYSTEM DOCS — Arquitetura do Backend Chat
## Especificação de Configuração, Mapeamento e Recursos

**Versão:** 1.0  
**Data:** 26 de junho de 2026  
**Pacote:** `com.chat`  
**Foco:** Integração React + Spring Boot | Arquitetura baseada em Clean Code + SOLID

---

## 1. Proposta de Árvore de Diretórios — Isolamento por Domínio

```
src/main/java/com/chat/
│
├── ChatApplication.java                        # Classe de inicialização Spring Boot
│
├── config/                                     # ⚙️ CONFIGURAÇÃO E INFRAESTRUTURA
│   ├── WebConfig.java                         # Beans de configuração HTTP (CORS, multipart, interceptadores)
│   ├── SecurityConfig.java                    # (Futuro) Configuração de autenticação/autorização
│   └── JpaConfig.java                         # (Futuro) Customizações de JPA/Hibernate
│
├── infrastructure/                             # 🔧 INFRAESTRUTURA TÉCNICA
│   ├── FileStorageService.java               # Persistência de arquivos (IO, naming strategy)
│   ├── FileParserService.java                # Extração de texto (PDF, TXT)
│   └── persistence/
│       ├── DatabaseInitializer.java          # Inicialização e seeding de dados
│       └── AuditListener.java                # (Futuro) Listeners JPA para auditoria
│
├── domain/                                     # 🏛️ DOMÍNIO DE NEGÓCIO (ISOLADO)
│   ├── entity/
│   │   ├── Session.java                       # Agregado raiz: Sessão de chat
│   │   ├── Message.java                       # Entidade: Mensagem dentro de uma sessão
│   │   └── FileMetadata.java                 # Value Object: Metadados de arquivo
│   │
│   └── service/                                # ✅ SERVIÇOS DE DOMÍNIO (Regras de Negócio)
│       ├── MessageDomainService.java          # Orquestração de lógica de mensagens
│       ├── SessionDomainService.java          # Orquestração de lógica de sessões
│       ├── FileProcessingDomainService.java   # Orquestração de processamento de arquivos
│       └── AssistantMockGenerator.java        # Geração de respostas mock do assistente
│
├── application/                                # 🎯 CAMADA DE APLICAÇÃO (Orchestração)
│   ├── dto/
│   │   ├── request/
│   │   │   ├── MessageRequest.java
│   │   │   ├── SessionRequest.java
│   │   │   └── FileUploadRequest.java         # Wrapper para multipart
│   │   │
│   │   └── response/
│   │       ├── MessageResponse.java
│   │       ├── SessionResponse.java
│   │       ├── UploadResponse.java
│   │       ├── HealthResponse.java
│   │       └── ApiErrorResponse.java
│   │
│   ├── mapper/                                 # 🔄 MAPEADORES DTO ↔️ Entity
│   │   ├── MessageMapper.java                 # Entity ↔️ DTO de Mensagens
│   │   ├── SessionMapper.java                 # Entity ↔️ DTO de Sessões
│   │   ├── FileMetadataMapper.java            # Entity ↔️ DTO de Metadados
│   │   └── BaseMapper.java                    # (Abstrato) Padrão para mapeadores
│   │
│   └── service/                                # 🚀 SERVIÇOS DE APLICAÇÃO (Use Cases)
│       ├── MessageApplicationService.java     # Caso de uso: Enviar/Listar mensagens
│       ├── SessionApplicationService.java     # Caso de uso: Criar/Listar sessões
│       └── FileUploadApplicationService.java  # Caso de uso: Upload e processamento
│
├── presentation/                               # 🌐 CAMADA DE APRESENTAÇÃO (Fronteira HTTP)
│   ├── controller/
│   │   ├── MessageController.java             # Endpoint: POST/GET mensagens
│   │   ├── SessionController.java             # Endpoint: POST/GET sessões
│   │   ├── UploadController.java              # Endpoint: POST upload multipart
│   │   └── HealthController.java              # Endpoint: GET health check
│   │
│   └── exception/
│       ├── GlobalExceptionHandler.java        # @ControllerAdvice para tratamento global
│       ├── SessionNotFoundException.java      # Exceção customizada
│       ├── InvalidFileTypeException.java      # Exceção customizada
│       ├── InactiveSessionException.java      # Exceção customizada
│       └── FileProcessingException.java       # Exceção customizada
│
├── data/                                       # 🗄️ CAMADA DE PERSISTÊNCIA
│   ├── entity/                                 # (Sincronizado com domain/entity)
│   │   ├── SessionEntity.java                 # Mapeamento JPA
│   │   ├── MessageEntity.java                 # Mapeamento JPA
│   │   └── FileMetadataEntity.java            # Mapeamento JPA
│   │
│   └── repository/
│       ├── SessionRepository.java             # Spring Data JPA
│       ├── MessageRepository.java             # Spring Data JPA
│       ├── FileMetadataRepository.java        # Spring Data JPA
│       └── CustomQueryRepository.java         # Queries complexas personalizadas
│
├── util/                                       # 🛠️ UTILITÁRIOS
│   ├── ValidationUtils.java                   # Validações reutilizáveis
│   ├── DateTimeUtils.java                     # Operações de data/hora
│   ├── FileUtils.java                         # Operações com arquivos
│   └── Constants.java                         # Constantes globais
│
└── event/                                      # 📡 EVENTOS (Futuro para event sourcing)
    ├── DomainEvent.java                       # Interface base para eventos
    ├── MessageSentEvent.java                  # Evento: Mensagem enviada
    └── FileUploadedEvent.java                 # Evento: Arquivo enviado

src/main/resources/
├── application.yml                            # Configuração principal (profiles)
├── application-dev.yml                        # Perfil de desenvolvimento
├── application-prod.yml                       # Perfil de produção
├── db/
│   └── migration/
│       ├── V1__init_schema.sql               # Flyway: Criação inicial de tabelas
│       └── V2__add_audit_columns.sql         # Flyway: Evolução do schema
└── static/
    └── index.html                             # (Opcional) SPA estática

```

---

## 2. Princípios Arquiteturais — Isolamento Rigoroso de Domínio

### 2.1 **Camadas e Suas Responsabilidades Exclusivas**

| Camada | Responsabilidade Exclusiva | O que NÃO faz | Exemplos de Componentes |
|--------|---------------------------|---------------|-------------------------|
| **Presentation** | Receber/validar requisições HTTP | Regras de negócio, persistência | Controllers, ExceptionHandlers |
| **Application** | Orquestrar use cases, converter DTOs | Lógica de domínio, acesso a dados | ApplicationServices, Mappers |
| **Domain** | Guardar TODA lógica de negócio | HTTP, DTOs, acesso a dados | DomainServices, Entities, ValueObjects |
| **Infrastructure** | Implementações técnicas (IO, BD) | Regras de negócio, orquestração | FileStorage, Databases, APIs externas |
| **Data** | Persistência (JPA, queries) | Lógica de negócio, HTTP | Repositories, Entities |

### 2.2 **Fluxo de Controle Obrigatório**

```
HTTP Request
    ↓
Presentation (Controller) → Valida parâmetros HTTP
    ↓
Application (ApplicationService) → Converte DTO, orquestra
    ↓
Domain (DomainService) → Executa lógica de negócio ✅ MONOPÓLIO
    ↓
Infrastructure/Data → Consulta/persiste dados
    ↓
Domain (DomainService) → Retorna resultado ao Application
    ↓
Application (Mapper) → Converte para DTO
    ↓
Presentation (Controller) → Retorna HTTP Response
```

### 2.3 **Regra de Ouro: Serviços de Domínio Não Conhecem HTTP**

```java
// ❌ VIOLAÇÃO — Serviço de Domínio acoplado a HTTP
@Service
public class MessageDomainService {
    public ResponseEntity<?> sendMessage(HttpServletRequest req) {  // ❌ HTTP
        // ...
    }
}

// ✅ CORRETO — Serviço de Domínio isolado
@Service
public class MessageDomainService {
    public MessageResult sendMessage(String content, UUID sessionId) {  // ✅ Domínio
        // Lógica pura
        return MessageResult.success(message);
    }
}
```

---

## 3. Componentes Críticos — Especificação Detalhada

### 3.1 **WebConfig.java** — Configuração de Infraestrutura HTTP

**Localização:** `com.chat.config`  
**Propósito:** Centralizar configurações HTTP, CORS, multipart e interceptadores.

**Responsabilidades:**
1. **CORS (Cross-Origin Resource Sharing)**
   - Permitir requisições do frontend React (ex: `http://localhost:5173`)
   - Configurar headers padrão: `Accept`, `Content-Type`, `Authorization`
   - Métodos permitidos: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`

2. **Multipart Upload**
   - Limite de tamanho de arquivo: 10 MB
   - Limite de requisição total: 10 MB
   - Encoding UTF-8 padrão

3. **Interceptadores/Filters (Futuro)**
   - Logging de requisições (input/output)
   - Validação de rate limiting
   - Headers de segurança (HSTS, X-Frame-Options, etc.)

4. **RestTemplate/WebClient (Futuro)**
   - Beans para chamadas síncronas/assíncronas a APIs externas
   - Timeout configurável
   - Retry logic

**Contrato de Configuração:**
```yaml
# application.yml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
      enabled: true

app:
  cors:
    allowed-origins: 
      - http://localhost:5173
      - http://localhost:3000
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600
```

**O que WebConfig NÃO faz:**
- Validar conteúdo de mensagens ❌
- Gerar respostas mock do assistente ❌
- Acessar banco de dados ❌

---

### 3.2 **MessageMapper.java** — Mapeamento Entity ↔️ DTO

**Localização:** `com.chat.application.mapper`  
**Padrão:** Strategy + Factory Pattern  
**Propósito:** Transformar dados entre camada de domínio e apresentação, garantindo encapsulamento.

**Responsabilidades:**
1. **Entity → DTO (Serialização)**
   ```
   Message (Entidade JPA) 
       ↓ 
   MessageResponse (DTO de saída)
   ```
   - Converte tipos de domínio para tipos externamente expostos
   - Remove campos sensíveis (ex: senhas, tokens internos)
   - Aplica transformações de formato (datas, enums)

2. **DTO → Entity (Desserialização)**
   ```
   MessageRequest (DTO de entrada)
       ↓
   Message (Entidade JPA)
   ```
   - Valida dados de entrada
   - Converte tipos apresentados para tipos de domínio
   - Garante integridade de relacionamentos (FK validation)

3. **Estratégia de Mapeamento**
   - Use `@Mapper` do **MapStruct** para performance (compile-time code generation)
   - Fallback: `ModelMapper` para casos complexos
   - Última opção: Mapeamento manual em métodos pequenos

**Contrato de API:**
```java
public interface MessageMapper {
    // Entity → Response
    MessageResponse toResponse(Message entity);
    List<MessageResponse> toResponseList(List<Message> entities);
    
    // Request → Entity
    Message toEntity(MessageRequest request);
    
    // Conversão de subentidades
    FileMetadataResponse toFileResponse(FileMetadata fileMetadata);
}
```

**Exemplo de Mapeamento com MapStruct:**
```java
@Mapper(componentModel = "spring")
public interface MessageMapper {
    
    @Mapping(source = "id", target = "id")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "createdAt", target = "timestamp", dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    MessageResponse toResponse(Message message);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Message toEntity(MessageRequest request);
}
```

**O que MessageMapper NÃO faz:**
- Gerar IDs ❌ (responsabilidade do serviço de domínio)
- Validar lógica de negócio ❌ (ex: "mensagem não pode estar vazia")
- Acessar banco de dados ❌
- Fazer chamadas a serviços de negócio ❌

**Isolamento de Domínio:**
- Mappers só conhecem DTOs e Entidades
- Não sabem da existência de Controllers nem de Repositories
- São testáveis em isolamento (unit tests simples)

---

### 3.3 **application.yml** — Configuração de Recursos

**Localização:** `src/main/resources/`  
**Propósito:** Centralizar todas as configurações da aplicação, com suporte a múltiplos ambientes.

**Seções Críticas:**

#### 3.3.1 Banco de Dados
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:chatdb          # H2 em memória (dev)
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop          # Recria schema a cada boot (dev)
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        jdbc:
          batch_size: 20
        order_inserts: true
```

**Ambientes:**
- **Dev (`application-dev.yml`):** H2 in-memory, DDL automático
- **Prod (`application-prod.yml`):** PostgreSQL, migrações Flyway

#### 3.3.2 Upload de Arquivos
```yaml
app:
  upload:
    dir: ${UPLOAD_DIR:./uploads}     # Diretório físico (env var override)
    max-file-size: 10485760          # 10 MB em bytes
    allowed-types: 
      - application/pdf
      - text/plain
    temp-dir: ${TEMP_DIR:./temp}
```

#### 3.3.3 CORS
```yaml
app:
  cors:
    allowed-origins: 
      - ${CORS_ORIGIN:http://localhost:5173}
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600
```

#### 3.3.4 Logging
```yaml
logging:
  level:
    root: INFO
    com.chat: DEBUG
    org.springframework.web: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n"
```

#### 3.3.5 Servidor
```yaml
server:
  port: 8080
  servlet:
    context-path: /api
    session:
      timeout: 30m
```

**Exemplo de `application-prod.yml`:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:chat_db}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate               # Não cria schema automaticamente
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQL10Dialect
        jdbc:
          batch_size: 50

  flyway:
    locations: classpath:db/migration
    baseline-on-migrate: true
```

**O que application.yml NÃO é:**
- ❌ Arquivo de lógica de negócio
- ❌ Local para credenciais hardcoded (usar variáveis de ambiente)
- ❌ Arquivo para configurações que mudam em tempo de execução (usar banco ou cache)

---

## 4. Tabela de Contratos — API e Responsabilidades

### 4.1 Contratos da API REST

| **Método** | **Endpoint** | **Responsabilidade Presentation** | **Responsabilidade Application** | **Responsabilidade Domain** | **HTTP Status** |
|-----------|------------|-----------------------------------|-----------------------------------|-----------------------------|-----------------|
| **GET** | `/api/health` | Receber GET; validar semânticamente | N/A | N/A (trivial) | 200 / 503 |
| **POST** | `/api/messages` | Validar DTO; invocar ApplicationService | Converter DTO → Entity; orquestrar DomainService | Criar Message; validar regras; gerar resposta mock | 201 / 400 / 404 |
| **GET** | `/api/messages/{sessionId}` | Validar UUID; invocar ApplicationService | Converter Entity list → DTO list | Recuperar mensagens ordenadas | 200 / 404 |
| **POST** | `/api/sessions` | Validar DTO; invocar ApplicationService | Converter DTO → Entity; orquestrar DomainService | Criar Session; gerar UUID | 201 / 400 |
| **GET** | `/api/sessions` | Validar paginação | Converter Entity list → DTO list | Recuperar todas as sessões | 200 |
| **POST** | `/api/upload` | Validar multipart; validar MIME type | Orquestrar DomainService + FileStorageService | Processar arquivo; extrair texto; validar tamanho | 201 / 400 / 413 |
| **GET** | `/api/files/{fileId}` | Validar UUID; invocar ApplicationService | Converter Entity → DTO | Recuperar metadados | 200 / 404 |

### 4.2 Divisão de Responsabilidades por Camada

#### **Presentation Layer (Controllers)**
```
MessageController.java
├── POST /api/messages
│   ├── @RequestBody @Valid MessageRequest
│   ├── Validar: não nulo, não vazio
│   ├── Chamar: messageApplicationService.sendMessage(request)
│   └── Retornar: 201 + JSON response
├── GET /api/messages/{sessionId}
│   ├── @PathVariable UUID sessionId
│   ├── @RequestParam(defaultValue="0") int page
│   ├── Validar: sessionId bem formado
│   ├── Chamar: messageApplicationService.getMessageHistory(sessionId, page)
│   └── Retornar: 200 + lista JSON
└── GlobalExceptionHandler
    ├── Capturar: SessionNotFoundException
    ├── Capturar: MethodArgumentNotValidException
    ├── Retornar: 404/400 + ApiErrorResponse
```

**O que Controllers NUNCA fazem:**
- ❌ Acesso direto a repositories
- ❌ Lógica de negócio complexa
- ❌ Transações explícitas
- ❌ Chamadas a APIs externas

#### **Application Layer (Services)**
```
MessageApplicationService.java
├── sendMessage(MessageRequest request): MessageResponseWrapper
│   ├── Converter: request → Message entity (via mapper)
│   ├── Validar: integridade referencial (session existe?)
│   ├── Chamar: messageDomainService.processMessage(message)
│   ├── Converter: resultado → MessageResponseWrapper (via mapper)
│   └── Retornar: wrapper com message do usuário + resposta do assistente
├── getMessageHistory(UUID sessionId, int page): PagedResponse
│   ├── Validar: sessionId existe (via repository)
│   ├── Chamar: messageRepository.findBySessionIdPaged(sessionId, page)
│   ├── Converter: entities → DTOs (via mapper)
│   └── Retornar: PagedResponse paginada
└── Mappers
    ├── toResponse(Message) → MessageResponse
    ├── toResponseList(List<Message>) → List<MessageResponse>
    ├── toEntity(MessageRequest) → Message
```

**O que ApplicationServices NUNCA fazem:**
- ❌ Criar DTOs manualmente (usar mappers)
- ❌ Acessar HTTP diretamente
- ❌ Chamadas de banco diretas (usar repositories)
- ❌ Lógica de negócio complexa (delegar para DomainServices)

#### **Domain Layer (DomainServices)**
```
MessageDomainService.java
├── processMessage(Message userMessage): MessageResult
│   ├── Validar: conteúdo não vazio OU arquivo presente
│   ├── Gerar: resposta mock (via AssistantMockGenerator)
│   ├── Criar: Message com role=ASSISTANT
│   ├── Retornar: MessageResult { userMessage, assistantMessage }
├── validateMessageContent(String content): boolean
├── checkSessionActive(UUID sessionId): void throws InactiveSessionException
```

**O que DomainServices NUNCA fazem:**
- ❌ Conhecer DTOs
- ❌ Conhecer Controllers
- ❌ Acessar HTTP
- ❌ Salvar diretamente em banco (retornar resultado para ApplicationService)

---

## 5. Fluxo de Responsabilidades — Exemplo Prático

### Cenário: POST /api/messages

```
1. HTTP Layer (Client)
   └─ POST /api/messages
      {
        "sessionId": "uuid-123",
        "role": "USER",
        "content": "Olá, tudo bem?"
      }

2. Presentation Layer (MessageController)
   ├─ @RequestBody @Valid MessageRequest request
   │  └─ Validações automáticas (não nulo, tamanho mínimo)
   ├─ messageApplicationService.sendMessage(request)
   └─ return ResponseEntity.status(201).body(response)

3. Application Layer (MessageApplicationService)
   ├─ Converter DTO → Entity
   │  └─ messageMapper.toEntity(request) → Message
   ├─ Validar integridade referencial
   │  └─ sessionRepository.findById(request.sessionId)
   │     └─ Se não existir: throw SessionNotFoundException
   ├─ Orquestrar lógica de negócio
   │  └─ messageDomainService.processMessage(message)
   └─ Converter resultado → DTO
      └─ messageMapper.toResponse(...) → MessageResponse

4. Domain Layer (MessageDomainService)
   ├─ messageDomainService.processMessage(userMessage)
   │  ├─ Validar: conteúdo não vazio
   │  ├─ Gerar resposta mock
   │  │  └─ assistantMockGenerator.generate(userMessage.getContent())
   │  ├─ Criar: Message { role: ASSISTANT, content: "..." }
   │  └─ Retornar: MessageResult { userMessage, assistantMessage }

5. Data Layer (Repositories)
   ├─ messageRepository.save(userMessage)
   └─ messageRepository.save(assistantMessage)

6. Application Layer (retorno)
   └─ Converter: MessageResult → MessageResponseWrapper

7. Presentation Layer (retorno)
   └─ HTTP 201 + JSON

8. HTTP Response (Client)
   {
     "userMessage": { "id": "...", "content": "Olá, tudo bem?" },
     "assistantMessage": { "id": "...", "content": "Olá! Como posso ajudar?" }
   }
```

---

## 6. Isolamento de Domínio — Princípios SOLID

### 6.1 **Single Responsibility Principle (SRP)**

Cada componente tem **UMA** razão para mudar:

| Componente | Razão para Mudar |
|-----------|-----------------|
| MessageController | Mudança no protocolo HTTP (ex: GraphQL, gRPC) |
| MessageApplicationService | Mudança em use cases (ex: adicionar paginação) |
| MessageDomainService | Mudança na lógica de negócio (ex: validações diferentes) |
| MessageMapper | Mudança na estrutura de DTO ou Entity |
| MessageRepository | Mudança no sistema de persistência (ex: MongoDB) |

### 6.2 **Open/Closed Principle (OCP)**

Componentes **abertos para extensão**, **fechados para modificação**:

```java
// ✅ CORRETO — Interface para extensão
public interface MessageStorage {
    void save(Message message);
    Message findById(UUID id);
}

public class DatabaseMessageStorage implements MessageStorage { ... }
public class CacheMessageStorage implements MessageStorage { ... }

// ❌ ERRADO — Modificar classe existente
public class MessageDomainService {
    if (storageType == "DATABASE") { ... }  // ❌ Modificação
    else if (storageType == "CACHE") { ... }
}
```

### 6.3 **Liskov Substitution Principle (LSP)**

Subtipos devem ser substituíveis sem quebrar o contrato:

```java
// ✅ CORRETO
public interface FileParser {
    String extract(File file) throws FileParsingException;
}

public class PdfFileParser implements FileParser { ... }
public class TextFileParser implements FileParser { ... }

// Ambas podem ser usadas intercambiadamente
FileParser parser = isPdf ? new PdfFileParser() : new TextFileParser();
parser.extract(file);  // Funciona para ambas
```

### 6.4 **Interface Segregation Principle (ISP)**

Interfaces específicas, não genéricas:

```java
// ❌ ERRADO — Interface grande e genérica
public interface MessageService {
    void sendMessage(...);
    void updateMessage(...);
    void deleteMessage(...);
    void uploadFile(...);
    void parseFile(...);
    void generateResponse(...);
}

// ✅ CORRETO — Interfaces segregadas
public interface MessageCommandService {
    void sendMessage(...);
    void updateMessage(...);
    void deleteMessage(...);
}

public interface MessageQueryService {
    List<Message> findBySessionId(...);
}

public interface FileProcessingService {
    void uploadFile(...);
}
```

### 6.5 **Dependency Inversion Principle (DIP)**

Depender de abstrações, não de implementações concretas:

```java
// ❌ ERRADO — Dependência concreta
@Service
public class MessageDomainService {
    private MessageRepository repository = new JpaMessageRepository();  // ❌
}

// ✅ CORRETO — Dependência abstrata (injeção)
@Service
public class MessageDomainService {
    private final MessageRepository repository;  // Interface
    
    public MessageDomainService(MessageRepository repository) {
        this.repository = repository;  // Injeção
    }
}
```

---

## 7. Checklist de Isolamento — Validação de Design

Use este checklist para verificar se o isolamento está rigoroso:

### Controllers
- [ ] Não contêm `@Autowired` para repositories?
- [ ] Não contêm lógica de negócio (if/loops complexos)?
- [ ] Todas as exceções de domínio são capturadas pelo GlobalExceptionHandler?
- [ ] Validações básicas (não nulo, tamanho) via `@Valid`?

### ApplicationServices
- [ ] Usam mappers para converter DTO ↔️ Entity?
- [ ] Orquestram múltiplos serviços (não fazem cálculos)?
- [ ] Têm `@Transactional` para transações?
- [ ] Retornam DTOs, nunca Entities?

### DomainServices
- [ ] Não conhecem DTOs?
- [ ] Não acessam repositories diretamente?
- [ ] Contêm toda lógica de negócio?
- [ ] São testáveis sem Spring (unit tests puros)?

### Mappers
- [ ] Convertem Entity ↔️ DTO?
- [ ] Não contêm lógica de validação?
- [ ] Não acessam banco?
- [ ] Usam MapStruct/ModelMapper?

### Repositories
- [ ] Só contêm queries (JPA, SQL)?
- [ ] Não contêm regras de negócio?
- [ ] Retornam Entities, não DTOs?

---

## 8. Matriz de Dependências — O que Pode Conhecer o Quê

```
┌─────────────────────────────────────────────────────────────┐
│  LAYER         │ CAN KNOW        │ CANNOT KNOW             │
├─────────────────────────────────────────────────────────────┤
│ Presentation  │ DTOs, HTTP      │ Repositories, Domain    │
│ Application   │ DTOs, Domain    │ HTTP, Repositories*     │
│ Domain        │ Entities        │ DTOs, HTTP, Repos       │
│ Data          │ Repositories    │ Domain, Services        │
│ Infrastructure│ Technical impl  │ Business Logic          │
└─────────────────────────────────────────────────────────────┘

* ApplicationService pode chamar Repository através de
  abstração, mas não direto — sempre via DomainService
```

---

## 9. Exemplo de Implementação — Isolamento em Ação

### Cenário: Adicionar novo validador de mensagem

**Requisito:** Não permitir mensagens com mais de 500 caracteres.

**Decisão Arquitetural:**
1. Validação de tamanho máximo → **Domain** (regra de negócio)
2. Onde adicionar? → **MessageDomainService** ou **Message entity**

**Implementação (sem quebrar isolamento):**

```java
// ✅ 1. Domain Layer — Adicionar validação
@Service
public class MessageDomainService {
    private static final int MAX_MESSAGE_LENGTH = 500;
    
    public MessageResult processMessage(Message userMessage) 
            throws MessageValidationException {
        
        validateMessageLength(userMessage.getContent());  // ✅ Isolado
        
        // ... resto da lógica
        return new MessageResult(userMessage, assistantMessage);
    }
    
    private void validateMessageLength(String content) 
            throws MessageValidationException {
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new MessageValidationException(
                "Mensagem não pode exceder " + MAX_MESSAGE_LENGTH + " caracteres"
            );
        }
    }
}

// ✅ 2. Application Layer — Apenas orquestra
@Service
public class MessageApplicationService {
    public MessageResponseWrapper sendMessage(MessageRequest request) {
        try {
            Message message = messageMapper.toEntity(request);
            MessageResult result = messageDomainService.processMessage(message);  // Exception lançada aqui
            return mapToWrapper(result);
        } catch (MessageValidationException e) {
            // Tratado pelo GlobalExceptionHandler
            throw e;
        }
    }
}

// ✅ 3. Presentation Layer — Apenas converte para HTTP
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MessageValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageValidation(
            MessageValidationException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiErrorResponse(
                "VALIDATION_ERROR",
                e.getMessage()
            ));
    }
}

// ✅ 4. DTOs — Sem mudanças (contrato externo preservado)
```

**O que mudou:**
- ✅ Apenas `MessageDomainService`
- ✅ Nenhuma mudança em Controllers
- ✅ Nenhuma mudança em DTOs (contrato HTTP preservado)
- ✅ Totalmente isolado e testável

---

## 10. Resumo Executivo — Guia Rápido

| Aspecto | Decisão |
|--------|---------|
| **Padrão Arquitetural** | Camadas (Presentation → Application → Domain → Data) + Clean Architecture |
| **Isolamento de Domínio** | DomainServices contêm TODA lógica; Controllers/Mappers não sabem de negócio |
| **Mapeamento Entity ↔️ DTO** | MapStruct (performance) ou ModelMapper (flexibilidade) |
| **Configuração HTTP** | WebConfig.java + application.yml (profiles dev/prod) |
| **Segurança de Camadas** | Injeção de dependência; interfaces para abstrair implementações |
| **Transações** | @Transactional em ApplicationServices (não em Controllers) |
| **Exceções** | GlobalExceptionHandler na Presentation; exceções customizadas do Domain |
| **Testes** | Unit tests em DomainServices; Integration tests em ApplicationServices |
| **Documentação** | Este SYSTEM_DOCS.md + JavaDoc em interfaces públicas |

---

## 11. Próximas Etapas — Roadmap Arquitetural

**Sprint Atual (1):**
- [ ] Implementar estrutura de pacotes conforme seção 1
- [ ] Criar WebConfig.java com CORS e multipart
- [ ] Criar MessageMapper.java com MapStruct
- [ ] Configurar application.yml com profiles
- [ ] Implementar GlobalExceptionHandler
- [ ] **Validação:** Todos os componentes seguem isolamento (seção 6)

**Sprint 2:**
- [ ] Adicionar autenticação/autorização (SecurityConfig)
- [ ] Implementar auditoria (AuditListener)
- [ ] Cache com Spring Cache (infrastructure)
- [ ] Testes unitários de DomainServices (coverage > 80%)

**Sprint 3:**
- [ ] Integração com modelo de IA real (substituir mock)
- [ ] Event sourcing (events/* para auditoria completa)
- [ ] Migrações Flyway automáticas (db/migration)
- [ ] Documentação Swagger/OpenAPI

---

## Apêndice A — Referências de Clean Code

- **Robert Martin (Uncle Bob):** Clean Code, Clean Architecture
- **Domain-Driven Design (DDD):** Implementação do isolamento
- **SOLID Principles:** Extensibilidade e manutenibilidade
- **Spring Best Practices:** Official Spring Documentation

---

**Documento preparado como Especificação Técnica**  
**Próximo passo:** Implementação conforme ESPECIFICACAO_BACKEND.md + SYSTEM_DOCS.md  
**Validação:** Execução de testes de coesão e acoplamento
