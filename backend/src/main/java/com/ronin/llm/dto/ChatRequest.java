package com.ronin.llm.dto;

public record ChatRequest(
        Long projectId,
        String message
) {}
