package com.ronin.llm.dto;

public record ImageGenerationRequest(
        String prompt,
        Long projectId,
        String provider,
        String model,
        String size,
        int quality
) {}
