package com.ronin.llm;

public record LLMEditInstruction(
        String path,
        String action,
        String content
) {}
