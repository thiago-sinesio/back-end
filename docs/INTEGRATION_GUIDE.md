# INTEGRATION_GUIDE.md — Guia de Integração dos Componentes

**Data:** 26 de junho de 2026  
**Versão:** 1.0  
**Foco:** WebConfig.java, MessageMapper.java e DTOs

---

## 1. Dependências Maven Necessárias

Adicione as seguintes dependências ao `pom.xml`:

```xml
<!-- Spring Boot Web (inclui REST, MVC, Servlet) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Boot Data JPA (Hibernate, persistência) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- H2 Database (desenvolvimento) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- MapStruct (mapeador Entity/DTO em compile-time) -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>

<!-- MapStruct Processor (gera código em tempo de compilação) -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>

<!-- Lombok (opcional, para reduzir boilerplate) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Spring Boot Validation (Bean Validation com Hibernate Validator) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Jackson (serialização/desserialização JSON) - já vem com spring-boot-starter-web -->
<!-- Se precisar versão específica: -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

---

## 2. Configuração do pom.xml — Plugins

Certifique-se de que o maven-compiler-plugin está configurado para usar Java 11+:

```xml
<build>
    <plugins>
        <!-- Compiler Plugin -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>11</source>
                <target>11</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>1.5.5.Final</version>
                    </path>
                    <!-- Se usar Lombok, adicionar também -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>

        <!-- Spring Boot Maven Plugin -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

---

## 3. Configuração do application.yml

Crie/atualize `src/main/resources/application.yml`:

```yaml
# ============================================================================
# Servidor
# ============================================================================
server:
  port: 8080
  servlet:
    context-path: /api

# ============================================================================
# Spring Boot - Configuração Geral
# ============================================================================
spring:
  # Banco de Dados (H2 para desenvolvimento)
  datasource:
    url: jdbc:h2:mem:chatdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true  # Acesso a http://localhost:8080/api/h2-console
  
  jpa:
    hibernate:
      ddl-auto: create-drop  # Recria schema a cada boot
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        jdbc:
          batch_size: 20
        order_inserts: true
  
  # Multipart Upload
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
      enabled: true
  
  # Jackson (serialização JSON)
  jackson:
    serialization:
      write-dates-as-timestamps: false  # ISO-8601 por padrão

# ============================================================================
# Aplicação - Configurações Customizadas
# ============================================================================
app:
  # CORS
  cors:
    allowed-origins: http://localhost:5173,http://localhost:3000
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600
  
  # Upload
  upload:
    dir: ./uploads
    max-file-size: 10485760  # 10 MB em bytes
    allowed-types: 
      - application/pdf
      - text/plain
    temp-dir: ./temp

# ============================================================================
# Logging
# ============================================================================
logging:
  level:
    root: INFO
    com.chat: DEBUG
    org.springframework.web: WARN
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n"
```

---

## 4. Estrutura de Pacotes — Criação de Diretórios

Certifique-se de que os seguintes pacotes existem:

```
src/main/java/com/chat/
├── config/
│   └── interceptor/
├── domain/
│   └── entity/
├── application/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   └── mapper/
├── presentation/
│   └── controller/
└── data/
    └── repository/
```

**Comandos para criar diretórios (PowerShell):**

```powershell
mkdir -Force src/main/java/com/chat/config/interceptor
mkdir -Force src/main/java/com/chat/domain/entity
mkdir -Force src/main/java/com/chat/application/dto/request
mkdir -Force src/main/java/com/chat/application/dto/response
mkdir -Force src/main/java/com/chat/application/mapper
mkdir -Force src/main/java/com/chat/presentation/controller
mkdir -Force src/main/java/com/chat/data/repository
```

---

## 5. Arquivos Criados — Resumo

| Arquivo | Pacote | Responsabilidade |
|---------|--------|-----------------|
| **WebConfig.java** | `com.chat.config` | CORS, multipart, interceptadores |
| **LoggingInterceptor.java** | `com.chat.config.interceptor` | Log de requisições HTTP |
| **MessageMapper.java** | `com.chat.application.mapper` | Mapeamento Entity ↔️ DTO |
| **MessageRequest.java** | `com.chat.application.dto.request` | DTO de entrada |
| **MessageResponse.java** | `com.chat.application.dto.response` | DTO de saída |
| **FileMetadataResponse.java** | `com.chat.application.dto.response` | DTO de metadados |
| **Message.java** | `com.chat.domain.entity` | Entidade JPA |
| **FileMetadata.java** | `com.chat.domain.entity` | Entidade JPA |

---

## 6. Injeção de Dependências — Como Usar

### 6.1 WebConfig — Auto-registrado

O `@Configuration` é automaticamente detectado pelo Spring Boot. Não precisa fazer nada especial.

```java
// Em qualquer componente, WebConfig será carregado automaticamente
// Não é necessário injetar WebConfig em nenhum lugar
```

### 6.2 MessageMapper — Injetar em Services

```java
@Service
public class MessageApplicationService {
    
    private final MessageMapper messageMapper;
    
    // Spring injeta a implementação gerada pelo MapStruct
    public MessageApplicationService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }
    
    public MessageResponse sendMessage(MessageRequest request) {
        // Converter DTO → Entity
        Message message = messageMapper.toEntity(request);
        
        // ... processar mensagem ...
        
        // Converter Entity → DTO
        return messageMapper.toResponse(message);
    }
}
```

### 6.3 Interceptadores — Auto-registrados via WebConfig

O `LoggingInterceptor` é registrado no `WebMvcConfigurer.addInterceptors()` do `WebConfig`. Funcionará automaticamente.

---

## 7. Validação — Testes Rápidos

### 7.1 Compilação

```bash
# Limpar build antigo
mvn clean

# Compilar (MapStruct vai gerar implementação)
mvn compile

# Observar se a implementação foi gerada em target/generated-sources/annotations/
ls target/generated-sources/annotations/com/chat/application/mapper/
```

**Esperado:** Um arquivo `MessageMapperImpl.java` será gerado automaticamente.

### 7.2 CORS — Testar Preflight

```bash
# Request OPTIONS (preflight)
curl -X OPTIONS http://localhost:8080/api/messages \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST" \
  -v

# Esperado: Header "Access-Control-Allow-Origin: http://localhost:5173"
```

### 7.3 Logging — Verificar Interceptador

```bash
# Fazer uma requisição qualquer
curl -X GET http://localhost:8080/api/health

# Esperado: No console do Spring, logs como:
# "→ [REQUEST] GET /api/health | Client: 127.0.0.1 | User-Agent: curl/..."
# "← [RESPONSE] GET /api/health | Status: 200 | Duration: 5ms ✓ Success"
```

---

## 8. Integração com Controllers — Exemplo

```java
@RestController
@RequestMapping("/messages")
public class MessageController {
    
    private final MessageApplicationService messageApplicationService;
    
    public MessageController(MessageApplicationService messageApplicationService) {
        this.messageApplicationService = messageApplicationService;
    }
    
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody @Valid MessageRequest request) {
        MessageResponse response = messageApplicationService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

---

## 9. Próximos Passos — Checklist

- [ ] Adicionar dependências ao `pom.xml`
- [ ] Configurar `application.yml`
- [ ] Criar estrutura de pacotes
- [ ] Copiar arquivos Java criados
- [ ] Executar `mvn clean compile`
- [ ] Verificar se `MessageMapperImpl` foi gerado
- [ ] Criar `MessageApplicationService`
- [ ] Criar `MessageController`
- [ ] Testes de CORS e logging
- [ ] Testes de mapeamento Entity ↔️ DTO

---

## 10. Troubleshooting

| Problema | Solução |
|----------|---------|
| `MessageMapperImpl` não é gerado | Verificar se MapStruct processor está em `annotationProcessorPaths` do pom.xml |
| Erro "Cannot find symbol: MessageMapperImpl" | Limpar (`mvn clean`) e recompilar (`mvn compile`) |
| CORS não funciona | Verificar se `WebConfig` foi carregado; verificar `application.yml` |
| Multipart upload rejeitado | Verificar se o tamanho é > 10MB; aumentar limite em `application.yml` |
| LoggingInterceptor não registra logs | Verificar nível de logging em `application.yml` (deve estar `DEBUG` para `com.chat`) |

---

## 11. Referências

- **MapStruct:** https://mapstruct.org/
- **Spring Boot CORS:** https://spring.io/guides/gs/spring-boot-cors/
- **Spring WebMvcConfigurer:** https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/config/annotation/WebMvcConfigurer.html
- **Jakarta/Javax Persistence:** https://jakarta.ee/specifications/persistence/

---

**Próxima etapa:** Implementar `MessageApplicationService`, `MessageController` e Repositories conforme SYSTEM_DOCS.md
