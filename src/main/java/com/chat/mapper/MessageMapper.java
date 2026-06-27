package com.chat.mapper;

import com.chat.application.dto.request.MessageRequest;
import com.chat.application.dto.response.FileMetadataResponse;
import com.chat.application.dto.response.MessageResponse;
import com.chat.domain.entity.FileMetadata;
import com.chat.domain.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * MessageMapper — responsible only for converting between domain entities and DTOs.
 *
 * This mapper keeps the domain layer isolated from transport concerns and keeps
 * controllers and services free from manual object transformation logic.
 */
@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "timestamp", source = "timestamp", qualifiedByName = "formatTimestamp")
    MessageResponse toResponse(Message message);

    List<MessageResponse> toResponseList(List<Message> messages);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "fileId", source = "fileId")
    Message toEntity(MessageRequest request);

    @Mapping(target = "fileId", source = "id")
    @Mapping(target = "uploadedAt", source = "uploadedAt", qualifiedByName = "formatTimestamp")
    FileMetadataResponse toFileResponse(FileMetadata fileMetadata);

    List<FileMetadataResponse> toFileResponseList(List<FileMetadata> fileMetadataList);

    @Named("formatTimestamp")
    static String formatTimestamp(LocalDateTime value) {
        if (value == null) {
            return null;
        }

        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }

    static UUID stringToUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    static String uuidToString(UUID value) {
        return value == null ? null : value.toString();
    }
}
