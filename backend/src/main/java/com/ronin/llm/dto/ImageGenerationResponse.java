package com.ronin.llm.dto;

public record ImageGenerationResponse(
        String imageUrl,
        String provider,
        String prompt,
        String revisedPrompt,
        String format,
        long generationTimeMs
) {}
