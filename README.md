# Back-end — Projeto de IA

Este repositório contém a implementação da API REST em Spring Boot responsável por gerenciar sessões de chat, fluxo de mensagens, recepção e processamento de documentos (PDF e TXT), e orquestração do domínio da aplicação.

## Tecnologias e Arquitetura
- **Java / Spring Boot**: Framework principal.
- **Isolamento de Domínio**: Arquitetura focada em desacoplamento. Controllers atuam exclusivamente como fronteiras HTTP, enquanto os Services detêm toda a regra de negócio.
- **Armazenamento**: Banco de dados relacional via Spring Data JPA (H2 em ambiente de desenvolvimento).
- **Processamento de Arquivos**: Suporte a envio de anexos via requisições `multipart/form-data`.

## Pré-requisitos
- Java 17 ou superior.
- Maven (opcional, caso não utilize o wrapper incluído no projeto).

## Como Executar

1. Navegue até o diretório do projeto:
   ```bash
   cd back-end
   ```

2. (Opcional) Configure as variáveis de ambiente necessárias no `application.yml` ou exporte-as no terminal.

3. Execute a aplicação utilizando o Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   *No Windows, utilize:*
   ```cmd
   mvnw.cmd spring-boot:run
   ```

4. A API estará rodando em `http://localhost:8080`.
5. Verifique o status da aplicação no endpoint de saúde:
   ```bash
   curl http://localhost:8080/api/health
   ```

## Documentação da Especificação
Para detalhes sobre contratos REST, estrutura de pastas e responsabilidade das camadas, consulte o arquivo [ESPECIFICACAO_BACKEND.md](./ESPECIFICACAO_BACKEND.md).
