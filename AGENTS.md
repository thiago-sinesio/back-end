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
