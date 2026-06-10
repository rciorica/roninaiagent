package com.ronin.llm.dto;

import java.util.List;

public record ChatRequest(
        Long projectId,
        String message,
        String actionType,
        String filePath,
        String activeEditorText,
        List<String> urls
) {}
