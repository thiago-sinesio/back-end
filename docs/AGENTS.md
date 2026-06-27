# Documentação de Agentes (AGENTS.md)

Este documento centraliza e rastreia todos os comandos (prompts) utilizados com ferramentas assistivas de IA para gerar o código e os artefatos estruturais deste repositório (Back-end). 

O projeto adota o **Spec-Driven Development (SDD)**, onde as interações com a IA respeitam o framework **CRISP** (Context, Role, Intent, Strictness, Parameters).

---

## Histórico de Prompts

*(Adicione abaixo os prompts utilizados em cada fase da implementação do back-end, seguindo o padrão CRISP. O formato deve refletir o contexto que orientou o agente a criar o código fonte associado.)*

### Exemplo de Template de Prompt de Codificação:
> **Aja como um** Arquiteto de Software Sênior especialista em Java/Spring Boot.
>
> **CONTEXTO:** 
> Estamos desenvolvendo o módulo de chat utilizando Spring Boot. A [ESPECIFICACAO_BACKEND.md](./ESPECIFICACAO_BACKEND.md) já foi aprovada. O foco agora é construir os endpoints base para Controller e a camada Service.
> 
> **INTENÇÃO:** 
> Baseado estritamente no documento de especificação, gere agora o código completo para os arquivos `MessageController.java` e `MessageService.java`.
> 
> **RESTRIÇÕES INEGOCIÁVEIS:**
> 1. Siga estritamente os princípios de Clean Code e SOLID.
> 2. O `MessageController` não deve conter NENHUMA lógica de negócio; todo tratamento e orquestração pertencem exclusivamente ao `MessageService`.
> 3. Documente os métodos complexos no Service, se houver.
> 
> **PARÂMETROS DE SAÍDA:**
> Entregue apenas os blocos de código com a devida declaração do nome do arquivo em comentários.


### Danielle - Prompt de Especificação (Gerou SYSTEM_DOCS.md)
Aja como um Arquiteto de Software Sénior especialista em Java e Spring Boot.

CONTEXTO:
Estamos desenvolvendo o módulo de backend da residência Serratec 2026.1 (Parte 1). O foco é a integração de React e Spring Boot, com foco na concepção arquitetural da "base da casa". Esta sprint abandona a codificação manual direta. O pacote do projeto é com.chat.

INTENÇÃO:
Crie o Documento de Especificação do Sistema (System Docs) para os componentes de configuração, mapeamento e recursos. Este documento deve servir como guia estrutural para a implementação técnica. Não escreva o código final.

RESTRIÇÕES INEGOCIÁVEIS:

Siga estritamente o isolamento rigoroso de domínio: Controladores são apenas fronteiras HTTP; Serviços detêm o monopólio do Domínio.

O design deve respeitar os princípios de Clean Code e SOLID.

Deve abordar os arquivos solicitados pela equipe: WebConfig.java (CORS/multipart), MessageMapper.java (Entity/DTO conversion) e as configurações em application.yml (banco, upload limits, CORS).

PARAMETROS DE SAÍDA:
Entregue a resposta em Markdown profissional contendo:

Uma proposta de árvore de diretórios demonstrando a organização dos arquivos.

Uma tabela de contratos de API/Lista de Responsabilidades detalhando o que será exposto e a responsabilidade de cada camada.

Uma breve explicação sobre a responsabilidade de cada componente para garantir que as regras de isolamento fiquem evidentes.

### Danielle - Prompt de Geração de Código Final (CORS, Mapper e YAML)
Aja como um Arquiteto de Software Sénior especialista em Java e Spring Boot.CONTEXTO:
Estamos desenvolvendo a "base da casa" do módulo de backend da residência Serratec 2026.1 (Parte 1). O design foi especificado e validado em SYSTEM_DOCS.md. O pacote base é com.chat.  INTENÇÃO:Baseado APENAS no documento de especificação (SYSTEM_DOCS.md) e no contexto fornecido, gere agora o código completo para os seguintes arquivos Java:com.chat.config.WebConfig.java (CORS, multipart upload, interceptadores).com.chat.mapper.MessageMapper.java (Entity <-> DTO conversion com MapStruct).RESTRIÇÕES INEGOCIÁVEIS:
1. Siga estritamente os princípios de Clean Code e SOLID.
2. O design deve respeitar o isolamento rigoroso de domínio: Controladores apenas expõem rotas HTTP; serviços detêm o monopólio do domínio.  PARAMETROS DE SAÍDA:
Forneça o código Java pronto para uso, incluindo pacotes e importações.

### Danielle - Geração do Arquivo YAML (Application.yml)
Aja como um Arquiteto de Software Sénior especialista em Java e Spring Boot.  CONTEXTO:
Estamos desenvolvendo a "base da casa" do módulo de backend da residência Serratec 2026.1 (Parte 1). O design foi especificado e validado em SYSTEM_DOCS.md. O pacote base é com.chat.  INTENÇÃO:Baseado APENAS no documento de especificação (SYSTEM_DOCS.md) e no contexto fornecido, gere agora o código completo e final para o seguinte arquivo estrutural:com.chat.mapper.MessageMapper.java (Entity <-> DTO conversão via MapStruct).RESTRIÇÕES INEGOCIÁVEIS:
1. Siga estritamente os princípios de Clean Code e SOLID.
2. Mantenha um isolamento rigoroso de domínio: Serviços detêm o monopólio do Domínio.  PARAMETROS DE SAÍDA:
Forneça o código pronto para uso, incluindo pacotes e importações.

### Danielle Carvalho - Prompt de Geração de Código YAML Completo (CORS e Multipart)
ja como um Arquiteto de Software Sénior especialista em Spring Boot e DevOps.CONTEXTO:
Estamos desenvolvendo o módulo de backend da residência Serratec 2026.1 (Parte 1). O foco é a concepção arquitetural da "base da casa". O design foi especificado e validado em SYSTEM_DOCS.md. O escopo exige banco relacional básico (H2/teste), CORS e multipart (recepção de documentos). O pacote base é com.chat.  INTENÇÃO:
Baseado estritamente no design arquitetural e nos requisitos técnicos, gere agora o arquivo de configuração application.yml completo, pronto para ser colocado em src/main/resources.  O arquivo deve contemplar:
1. Configurações de banco de dados H2/teste (perfis dev/prod).
2. Configurações explícitas de CORS para frontend em http://localhost:5173.
3. Configurações de multipart para upload de arquivos nos formatos .txt e .pdf, com limites de 10MB (max-file-size e max-request-size), conforme image_aee8fe.png.  RESTRIÇÕES INEGOCIÁVEIS:
1. Siga estritamente Clean Code e SOLID.
2. Não utilize segredos ou chaves reais.  PARAMETROS DE SAÍDA:
Entregue o código YAML pronto e com comentários explicando cada seção para Clean Code.

### Danielle Carvalho - Validação de Entrega de Part 1 (Design Validado e Arquivos Gerados)

Documentação Validada: Confirmo que o SYSTEM_DOCS.md foi gerado e validado via IA do OpenCode em total conformidade com os princípios CRISP e o design arquitetural.

Artefatos Gerados: Confirmo a geração integral e a integração técnica dos arquivos estruturais e de base exigidos na imagem image_aee8fe.png, incluindo: application.yml, MessageMapper.java, UserMapper.java, WebConfig.java e LoggingInterceptor.java.

Conformidade de Requisitos: Valido que as configurações de CORS eMultipart (limites de 10MB para .pdf e .txt) foram implementadas conforme exigido. Os arquivos Java respeitam os isolamentos de domínio e os princípios Clean Code e SOLID definidos na especificação.
Danielle Carvalho