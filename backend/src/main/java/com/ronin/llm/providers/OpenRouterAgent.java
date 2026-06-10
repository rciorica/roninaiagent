package com.ronin.llm.providers;

import com.ronin.llm.LLMAgent;
import com.ronin.llm.LLMResult;
import com.ronin.llm.WebSearchService;
import com.ronin.llm.WebSearchTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenRouterAgent implements LLMAgent {

    private final OpenRouterClient openRouterClient;
    private final WebSearchService webSearchService;
    private final WebSearchTool webSearchTool;

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
        String enhancedPrompt = prompt;
        try {
            Optional<String> searchQuery = webSearchService.extractSearchQuery(prompt);
            if (searchQuery.isPresent()) {
                String searchResults = webSearchTool.searchTheWeb(searchQuery.get());
                enhancedPrompt = "You have access to a tool named WebSearchTool. " +
                        "When asked for current facts, news, or up-to-date information, use it.\n" +
                        "WebSearchTool results for: " + searchQuery.get() + "\n" +
                        searchResults + "\n\n" +
                        prompt;
            }
        } catch (Exception e) {
            log.warn("Failed to execute WebSearchTool for OpenRouter model: {}", e.getMessage());
        }

        OpenRouterResult result = openRouterClient.generate(providerName, enhancedPrompt);
        return new LLMResult(result.getContent(), result.getTokensUsed());
    }
}
