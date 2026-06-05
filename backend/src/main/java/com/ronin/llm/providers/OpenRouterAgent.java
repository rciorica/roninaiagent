package com.ronin.llm.providers;

import com.ronin.llm.LLMAgent;
import com.ronin.llm.LLMResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenRouterAgent implements LLMAgent {

    private final OpenRouterClient openRouterClient;

    @Override
    public String getName() {
        return "OpenRouter";
    }

    @Override
    public boolean supports(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            return false;
        }
        String normalized = providerName.trim().toLowerCase();
        return normalized.startsWith("llama")
                || normalized.startsWith("mistral")
                || normalized.startsWith("openrouter")
                || normalized.contains("openrouter")
                || normalized.contains("llama-3")
                || normalized.contains("mistral-7b");
    }

    @Override
    public LLMResult generate(String providerName, String prompt) {
        OpenRouterResult result = openRouterClient.generate(providerName, prompt);
        return new LLMResult(result.getContent(), result.getTokensUsed());
    }
}
