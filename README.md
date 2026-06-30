# 🧠 Back-end — Chat Assistant (Projeto de IA)

O back-end deste projeto é uma robusta **API RESTful** construída com **Java 17** e **Spring Boot 3**. Ele atua como o cérebro da aplicação, orquestrando sessões de conversa, processando o fluxo de mensagens, e gerenciando o upload e a extração inteligente de textos de documentos (PDFs e TXTs) para posterior integração com Inteligência Artificial.

---

## ✨ Principais Funcionalidades

- **Gerenciamento de Sessões (`Session`)**: Criação, listagem e controle de conversas independentes.
- **Mensageria Contextual (`Message`)**: Armazenamento de histórico com separação clara de papéis (`USER` e `ASSISTANT`).
- **Processamento RAG Integrado**:
  - Upload via `multipart/form-data`
  - Limites de tamanho configuráveis (padrão de 10MB)
  - Extração nativa de texto de PDFs utilizando o **Apache PDFBox**.
  - Chunking e Vetorização automática usando IA Local.
- **Integração com Ollama**: Roda offline utilizando modelos locais (Llama 3.2 e Nomic Embed Text).
- **Integração com Postgres pgvector**: Armazena e busca blocos de texto por similaridade matemática.
- **Tratamento de Exceções Global**: Padronização de respostas de erro da API com `@ControllerAdvice`.
- **CORS Configurado Nativamente**: Pronto para se conectar de forma segura ao Front-end sem barreiras de rede.

---

## 🏗 Arquitetura e Tecnologias

Este projeto segue um modelo de **Isolamento de Domínio**, visando alto desacoplamento e manutenibilidade:

* **☕ Java 17** + **Spring Boot 3.4.4**
* **💾 Spring Data JPA** + **PostgreSQL** (com extensão `pgvector`)
* **🧠 Ollama** (IA 100% Offline via API compatível com OpenAI)
* **📄 Apache PDFBox** (Processamento avançado de PDFs)
* **🛠 MapStruct** (Para mapeamento ágil entre Entidades e DTOs)

*A arquitetura do banco de dados agora utiliza o próprio **Hibernate (`ddl-auto: update`)** para simplificar a criação das tabelas automaticamente sem a necessidade de migrações complexas.*

### 📂 Estrutura de Diretórios
```
src/main/java/com/chat/
├── client/        # Clientes HTTP (AiClient para o Ollama)
├── config/        # Configurações do Spring (CORS, Interceptors, Inicializadores)
├── controller/    # Endpoints HTTP (fronteira da API)
├── dto/           # Objetos de Transferência de Dados (Payloads limpos)
├── exception/     # Handlers Globais e Exceções customizadas
├── model/         # Entidades de Domínio (JPA)
├── repository/    # Interfaces de comunicação com o Banco de Dados
└── service/       # Regras de Negócio e Lógica da Aplicação (RAG)
```

---

## 🚀 Como Executar Localmente

### Pré-requisitos
- Java 17 (JDK)
- Docker Desktop (Para subir o PostgreSQL)
- Ollama (Para rodar a inteligência artificial localmente)

### Passo a passo

1. **Baixe os Modelos da IA Local (Ollama):**
   No seu terminal, rode os comandos:
   ```bash
   ollama pull llama3.2
   ollama pull nomic-embed-text
   ```

2. **Inicie o Banco de Dados:**
   Volte para a pasta raiz do projeto (fora de back-end) e suba o container do Postgres:
   ```bash
   docker-compose up -d
   ```

3. **Inicie a aplicação utilizando o Maven:**
   No diretório do `back-end`, rode no Windows:
   ```cmd
   mvn.cmd spring-boot:run
   ```
   *Nota: O Hibernate criará as tabelas do PostgreSQL e a extensão pgvector automaticamente durante o primeiro start.*

4. **Verifique se a API está online:**
   O Tomcat iniciará na porta `8080`. Você pode verificar o status do servidor acessando o endpoint:
   ```bash
   curl http://localhost:8080/api/health
   ```

---

## 📚 Documentação Adicional

Todos os detalhes específicos de arquitetura, especificações técnicas detalhadas, diagramas e guia de integração estão disponíveis na pasta `docs/`. Recomendamos fortemente a leitura dos seguintes arquivos:

- 📄 [Especificação do Back-end](./docs/ESPECIFICACAO_BACKEND.md)
- 📄 [Guia de Integração (API Contracts)](./docs/INTEGRATION_GUIDE.md)
- 📄 [Documentação do Sistema (Arquitetura Completa)](./docs/SYSTEM_DOCS.md)
