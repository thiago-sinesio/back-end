package com.chat.service;

import org.springframework.stereotype.Service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

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
        try (PDDocument document = Loader.loadPDF(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao extrair texto do PDF: " + e.getMessage(), e);
        }
    }
}
