package com.chat.service;

import com.chat.dto.MessageRequest;
import com.chat.dto.MessageResponse;
import com.chat.model.Message;
import com.chat.model.Role;
import com.chat.model.Session;
import com.chat.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final SessionService sessionService;

    public MessageService(MessageRepository messageRepository,
                          SessionService sessionService) {
        this.messageRepository = messageRepository;
        this.sessionService = sessionService;
    }

    @Transactional
    public Map<String, MessageResponse> sendMessage(MessageRequest request) {
        if (request.role() == null || (!"USER".equalsIgnoreCase(request.role()) && !"ASSISTANT".equalsIgnoreCase(request.role()))) {
            throw new IllegalArgumentException("Role deve ser USER ou ASSISTANT");
        }

        if ((request.content() == null || request.content().isBlank()) && request.fileId() == null) {
            throw new IllegalArgumentException("Content não pode ser vazio sem fileId");
        }

        Session session;
        try {
            session = sessionService.findActiveSession(request.sessionId());
        } catch (com.chat.exception.SessionNotFoundException e) {
            session = sessionService.createSession(null);
        }

        UUID sessionId = session.getId();
        Role role = Role.valueOf(request.role().toUpperCase());

        Message userMessage = new Message(sessionId, role, request.content(), request.fileId());
        userMessage = messageRepository.save(userMessage);

        String mockResponse = generateMockResponse(request.content());
        Message assistantMessage = new Message(sessionId, Role.ASSISTANT, mockResponse, null);
        assistantMessage = messageRepository.save(assistantMessage);

        sessionService.updateActivity(sessionId);

        Map<String, MessageResponse> result = new LinkedHashMap<>();
        result.put("userMessage", toResponse(userMessage));
        result.put("assistantMessage", toResponse(assistantMessage));
        return result;
    }

    public Map<String, Object> getHistory(UUID sessionId, int page, int size, String sort) {
        Session session = sessionService.findById(sessionId);

        Sort sorting = Sort.by(Sort.Direction.ASC, "timestamp");
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Message> messagePage = messageRepository.findBySessionId(sessionId, pageable);

        List<MessageResponse> messages = messagePage.getContent().stream()
                .map(this::toResponse)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", sessionId.toString());
        result.put("messages", messages);
        result.put("page", messagePage.getNumber());
        result.put("size", messagePage.getSize());
        result.put("totalElements", messagePage.getTotalElements());
        result.put("totalPages", messagePage.getTotalPages());
        return result;
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSessionId(),
                message.getRole().name(),
                message.getContent(),
                message.getFileId(),
                message.getTimestamp()
        );
    }

    String generateMockResponse(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Recebi sua mensagem. Em breve serei integrado a um modelo de IA para respostas mais inteligentes!";
        }

        String lower = userMessage.toLowerCase();

        if (lower.contains("olá") || lower.contains("oi")) {
            return "Olá! Sou o assistente virtual. Como posso ajudar?";
        }
        if (lower.contains("ajuda")) {
            return "Posso te ajudar com informações gerais. Envie sua pergunta ou anexe um documento PDF/TXT para análise.";
        }
        if (lower.contains("arquivo") || lower.contains("documento")) {
            return "Para enviar um documento, use a área de arrastar-e-soltar ou o botão de upload. Aceito arquivos PDF e TXT de até 10MB.";
        }

        String snippet = userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage;
        return "Recebi sua mensagem: '" + snippet + "'. Em breve serei integrado a um modelo de IA para respostas mais inteligentes!";
    }
}
