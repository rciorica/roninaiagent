package com.ronin.llm;

public record ModelSelectionResult(
        LLMProviderEntity chosen,
        LLMProviderEntity preferred,
        boolean switched
) {}
