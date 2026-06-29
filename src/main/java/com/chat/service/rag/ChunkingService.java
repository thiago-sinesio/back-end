package com.chat.service.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private final int chunkSize;
    private final int chunkOverlap;

    public ChunkingService(
            @Value("${app.rag.chunk.size:1000}") int chunkSize,
            @Value("${app.rag.chunk.overlap:200}") int chunkOverlap) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            if (end < text.length()) {
                int breakPoint = findBreakPoint(text, end, start);
                if (breakPoint > start) {
                    end = breakPoint;
                }
            }
            chunks.add(text.substring(start, end).trim());
            start = end - chunkOverlap;
            if (start >= text.length()) {
                break;
            }
            if (start + chunkOverlap > text.length() && !chunks.isEmpty()) {
                break;
            }
        }
        return chunks;
    }

    private int findBreakPoint(String text, int from, int lowerBound) {
        int newline = text.lastIndexOf('\n', from - 1);
        if (newline > lowerBound) {
            return newline + 1;
        }
        int period = text.lastIndexOf(". ", from - 1);
        if (period > lowerBound) {
            return period + 2;
        }
        int space = text.lastIndexOf(' ', from - 1);
        if (space > lowerBound) {
            return space + 1;
        }
        return from;
    }
}
