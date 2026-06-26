package com.chat.application.mapper;

import com.chat.domain.entity.FileMetadata;
import com.chat.domain.entity.Message;
import com.chat.application.dto.request.MessageRequest;
import com.chat.application.dto.response.FileMetadataResponse;
import com.chat.application.dto.response.MessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * MessageMapper — Mapeador Entity ↔️ DTO com MapStruct
 * 
 * Padrão: Strategy + Factory Pattern
 * Compilador: MapStruct (compile-time code generation para performance)
 * 
 * Responsabilidades:
 * 1. Entity → DTO (Serialização)
 *    - Message (entidade JPA) → MessageResponse (DTO de saída)
 *    - Remove campos sensíveis
 *    - Transforma formatos (LocalDateTime → ISO-8601 String)
 * 
 * 2. DTO → Entity (Desserialização)
 *    - MessageRequest (DTO de entrada) → Message (entidade JPA)
 *    - Valida integridade referencial
 *    - Converte tipos (String → UUID, se necessário)
 * 
 * 3. Subentidades
 *    - FileMetadata ↔️ FileMetadataResponse
 * 
 * Princípios SOLID:
 * - Single Responsibility: Apenas conversão de tipos
 * - Open/Closed: Fácil adicionar novos mappers via composição
 * - Liskov Substitution: Contrato de interface respeitado
 * - Interface Segregation: Métodos pequenos e específicos
 * - Dependency Inversion: Injeção de dependência (componentModel = "spring")
 * 
 * O que NÃO faz:
 * ✗ Gerar IDs (responsabilidade do serviço de domínio)
 * ✗ Validar lógica de negócio (ex: "mensagem não pode estar vazia")
 * ✗ Acessar banco de dados
 * ✗ Fazer chamadas a serviços de negócio
 * ✗ Lançar exceções customizadas
 * 
 * Isolamento de Domínio:
 * - Mappers são ágnosticos de Controllers e Repositories
 * - Conhecem apenas DTOs e Entidades
 * - Testáveis em isolamento (unit tests simples)
 */
@Mapper(
        componentModel = "spring",  // Gera Spring @Component automaticamente
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,  // Verifica null em conversão
        imports = {LocalDateTime.class, DateTimeFormatter.class}  // Imports para expressões
)
public interface MessageMapper {

    // ============================================================================
    // Entity → Response (Serialização)
    // ============================================================================

    /**
     * Converte uma Message (entidade JPA) em MessageResponse (DTO de saída)
     * 
     * Transformações:
     * - id: UUID → String (JSON-friendly)
     * - sessionId: UUID → String
     * - role: Enum → String (para compatibilidade)
     * - timestamp: LocalDateTime → ISO-8601 String (yyyy-MM-dd'T'HH:mm:ss'Z')
     * - fileId: UUID → String (nullable)
     * 
     * @param message Entidade JPA de mensagem
     * @return DTO de resposta HTTP
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "fileId", source = "fileId")
    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "localDateTimeToString")
    MessageResponse toResponse(Message message);

    /**
     * Converte uma lista de Message em lista de MessageResponse
     * Usado para retorno paginado ou bulk de mensagens
     * 
     * @param messages Lista de entidades JPA
     * @return Lista de DTOs de resposta
     */
    List<MessageResponse> toResponseList(List<Message> messages);

    // ============================================================================
    // Request → Entity (Desserialização)
    // ============================================================================

    /**
     * Converte um MessageRequest (DTO de entrada) em Message (entidade JPA)
     * 
     * Mapeamentos:
     * - sessionId: String → UUID (String vem do JSON)
     * - role: String (enum validation ocorre no DTO ou no serviço)
     * - content: String → String
     * - id: Ignorado (será gerado pelo serviço)
     * - timestamp: Ignorado (será gerado pelo serviço com LocalDateTime.now())
     * - fileId: Pode vir como String, convertido para UUID se necessário
     * 
     * Nota: Validações de negócio (ex: "conteúdo não vazio") são feitas
     *       no serviço de domínio, NÃO no mapper.
     * 
     * @param request DTO de entrada (vem do JSON do cliente)
     * @return Entidade JPA pronta para persistência
     */
    @Mapping(target = "id", ignore = true)  // Gerado pelo serviço
    @Mapping(target = "timestamp", ignore = true)  // Gerado pelo serviço com LocalDateTime.now()
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "fileId", source = "fileId", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    Message toEntity(MessageRequest request);

    // ============================================================================
    // Subentidades - FileMetadata
    // ============================================================================

    /**
     * Converte FileMetadata (entidade JPA) em FileMetadataResponse (DTO de saída)
     * 
     * Transformações:
     * - id: UUID → String
     * - uploadedAt: LocalDateTime → ISO-8601 String
     * 
     * @param fileMetadata Entidade JPA de metadados
     * @return DTO de resposta
     */
    @Mapping(target = "fileId", source = "id")  // Renomeia 'id' para 'fileId' na resposta
    @Mapping(target = "originalName", source = "originalName")
    @Mapping(target = "mimeType", source = "mimeType")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "extractedText", source = "extractedText")
    @Mapping(target = "uploadedAt", source = "uploadedAt", qualifiedByName = "localDateTimeToString")
    FileMetadataResponse toFileResponse(FileMetadata fileMetadata);

    /**
     * Converte lista de FileMetadata em lista de FileMetadataResponse
     * 
     * @param fileMetadataList Lista de entidades
     * @return Lista de DTOs
     */
    List<FileMetadataResponse> toFileResponseList(List<FileMetadata> fileMetadataList);

    // ============================================================================
    // Conversão de Tipos (Qualificadores)
    // ============================================================================

    /**
     * Converte LocalDateTime em String ISO-8601
     * Formato: yyyy-MM-dd'T'HH:mm:ss'Z' (UTC)
     * 
     * Exemplo:
     * - Input: LocalDateTime.of(2026, 6, 25, 10, 30, 0)
     * - Output: "2026-06-25T10:30:00Z"
     * 
     * @param localDateTime Data/hora local
     * @return String em formato ISO-8601
     */
    static String localDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return localDateTime.format(formatter);
    }

    /**
     * Converte String UUID em UUID
     * Usado quando a entrada é uma string que precisa ser UUID
     * 
     * @param uuidString String de UUID
     * @return UUID parseado, ou null se inválido
     */
    static UUID stringToUuid(String uuidString) {
        if (uuidString == null || uuidString.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            // Em caso de erro, retornar null (será validado no serviço)
            return null;
        }
    }

    /**
     * Converte UUID em String
     * 
     * @param uuid UUID a converter
     * @return String de UUID, ou null se entrada for null
     */
    static String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }
}
