package com.chat.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class FileParserService {

    public String parseText(byte[] content, String mimeType) {
        if ("text/plain".equals(mimeType)) {
            return new String(content, StandardCharsets.UTF_8);
        }
        if ("application/pdf".equals(mimeType)) {
            return extractTextFromPdf(content);
        }
        throw new IllegalArgumentException("Tipo de arquivo não suportado: " + mimeType);
    }

    private String extractTextFromPdf(byte[] content) {
        // TODO: Implementar extração de texto de PDF com Apache PDFBox
        // A dependência precisa ser adicionada ao pom.xml
        return "[Texto extraído do PDF - " + content.length + " bytes]";
    }
}
