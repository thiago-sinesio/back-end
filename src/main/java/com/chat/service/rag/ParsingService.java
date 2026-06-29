package com.chat.service.rag;

import com.chat.service.FileParserService;
import org.springframework.stereotype.Service;

@Service
public class ParsingService {

    private final FileParserService fileParserService;

    public ParsingService(FileParserService fileParserService) {
        this.fileParserService = fileParserService;
    }

    public String parse(byte[] content, String mimeType) {
        return fileParserService.parseText(content, mimeType);
    }
}
