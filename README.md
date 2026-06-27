# 🧠 Back-end — Chat Assistant (Projeto de IA)

O back-end deste projeto é uma robusta **API RESTful** construída com **Java 17** e **Spring Boot 3**. Ele atua como o cérebro da aplicação, orquestrando sessões de conversa, processando o fluxo de mensagens, e gerenciando o upload e a extração inteligente de textos de documentos (PDFs e TXTs) para posterior integração com Modelos de Inteligência Artificial.

---

## ✨ Principais Funcionalidades

- **Gerenciamento de Sessões (`Session`)**: Criação, listagem e controle de conversas independentes.
- **Mensageria Contextual (`Message`)**: Armazenamento de histórico com separação clara de papéis (`USER` e `ASSISTANT`).
- **Anexos e Documentos (`FileMetadata`)**: 
  - Upload via `multipart/form-data`
  - Limites de tamanho configuráveis (padrão de 10MB)
  - Extração nativa de texto de PDFs utilizando o **Apache PDFBox**.
- **Tratamento de Exceções Global**: Padronização de respostas de erro da API com `@ControllerAdvice`.
- **CORS Configurado Nativamente**: Pronto para se conectar de forma segura ao Front-end sem barreiras de rede.

---

## 🏗 Arquitetura e Tecnologias

Este projeto segue um modelo de **Isolamento de Domínio**, visando alto desacoplamento e manutenibilidade:

* **☕ Java 17** + **Spring Boot 3.4.4**
* **💾 Spring Data JPA** + **H2 Database** (para rápido desenvolvimento em memória, com perfil de migração fácil para PostgreSQL)
* **📄 Apache PDFBox** (Processamento avançado de PDFs)
* **🛠 MapStruct** (Para mapeamento ágil entre Entidades e DTOs)

### 📂 Estrutura de Diretórios
```
src/main/java/com/chat/
├── config/        # Configurações do Spring (CORS, Interceptors, Multipart)
├── controller/    # Endpoints HTTP (fronteira da API)
├── dto/           # Objetos de Transferência de Dados (Payloads limpos)
├── exception/     # Handlers Globais e Exceções customizadas
├── model/         # Entidades de Domínio (JPA)
├── repository/    # Interfaces de comunicação com o Banco de Dados
└── service/       # Regras de Negócio e Lógica da Aplicação
```

---

## 🚀 Como Executar Localmente

### Pré-requisitos
- Java 17 (JDK)
- Apache Maven (Opcional, pois o projeto possui o Maven Wrapper incluído)

### Passo a passo

1. **Clone e acesse o diretório do back-end:**
   ```bash
   cd back-end
   ```

2. **Inicie a aplicação utilizando o Maven:**
   No Windows:
   ```cmd
   mvn.cmd spring-boot:run
   ```
   No Linux/Mac:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Verifique se a API está online:**
   O Tomcat iniciará na porta `8080`. Você pode verificar o status do servidor acessando o endpoint:
   ```bash
   curl http://localhost:8080/api/health
   ```

4. **Acesse o Banco de Dados (H2 Console):**
   - **URL:** `http://localhost:8080/h2-console`
   - **JDBC URL:** `jdbc:h2:mem:chatdb`
   - **User:** `sa`
   - **Password:** *(deixe em branco)*

---

## 📚 Documentação Adicional

Todos os detalhes específicos de arquitetura, especificações técnicas detalhadas, diagramas e guia de integração estão disponíveis na pasta `docs/`. Recomendamos fortemente a leitura dos seguintes arquivos:

- 📄 [Especificação do Back-end](./docs/ESPECIFICACAO_BACKEND.md)
- 📄 [Guia de Integração (API Contracts)](./docs/INTEGRATION_GUIDE.md)
- 📄 [Documentação do Sistema (Arquitetura Completa)](./docs/SYSTEM_DOCS.md)
