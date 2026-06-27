package com.chat.service;

import com.chat.dto.UploadResponse;
import com.chat.model.FileMetadata;
import com.chat.model.Message;
import com.chat.model.Role;
import com.chat.repository.MessageRepository;
import com.chat.repository.SessionRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf", "text/plain"
    );

    private final Path uploadDir;
    private final FileParserService fileParserService;
    private final MessageRepository messageRepository;
    private final SessionRepository sessionRepository;
    private final EntityManager entityManager;

    public FileStorageService(
            @Value("${app.upload.dir:./uploads}") String uploadDirPath,
            FileParserService fileParserService,
            MessageRepository messageRepository,
            SessionRepository sessionRepository,
            EntityManager entityManager) {
        this.uploadDir = Paths.get(uploadDirPath);
        this.fileParserService = fileParserService;
        this.messageRepository = messageRepository;
        this.sessionRepository = sessionRepository;
        this.entityManager = entityManager;
        initUploadDir();
    }

    private void initUploadDir() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de upload: " + uploadDir, e);
        }
    }

    public void validateFileType(String mimeType) {
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new com.chat.exception.InvalidFileTypeException(
                    "Tipo de arquivo não suportado. Envie PDF ou TXT.");
        }
    }

    @Transactional
    public UploadResponse storeFile(byte[] fileContent, String originalName, String mimeType, long size, UUID sessionId) {
        validateFileType(mimeType);

        if (!sessionRepository.existsById(sessionId)) {
            throw new com.chat.exception.SessionNotFoundException(sessionId);
        }

        String extension = extractExtension(originalName);
        String storedName = UUID.randomUUID() + extension;
        Path targetPath = uploadDir.resolve(storedName);

        try {
            Files.write(targetPath, fileContent);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo no disco", e);
        }

        String extractedText = fileParserService.parseText(fileContent, mimeType);

        FileMetadata metadata = new FileMetadata(originalName, storedName, mimeType, size);
        metadata.setExtractedText(extractedText);
        entityManager.persist(metadata);

        Message autoMessage = new Message(sessionId, Role.USER, "[Arquivo enviado: " + originalName + "]", metadata.getId());
        messageRepository.save(autoMessage);

        return new UploadResponse(
                metadata.getId(),
                metadata.getOriginalName(),
                metadata.getMimeType(),
                metadata.getSize(),
                metadata.getExtractedText(),
                metadata.getUploadedAt()
        );
    }

    public UploadResponse getFileMetadata(UUID fileId) {
        FileMetadata metadata = entityManager.find(FileMetadata.class, fileId);
        if (metadata == null) {
            throw new RuntimeException("Arquivo não encontrado: " + fileId);
        }
        return new UploadResponse(
                metadata.getId(),
                metadata.getOriginalName(),
                metadata.getMimeType(),
                metadata.getSize(),
                metadata.getExtractedText(),
                metadata.getUploadedAt()
        );
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? "" : fileName.substring(dotIndex);
    }
}
