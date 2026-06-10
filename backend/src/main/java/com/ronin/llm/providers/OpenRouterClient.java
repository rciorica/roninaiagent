package com.ronin.llm.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronin.config.OpenRouterProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class OpenRouterClient {

    private final WebClient.Builder webClientBuilder;
    private final String apiKey;
    private final String apiUrl;

    public OpenRouterClient(WebClient.Builder webClientBuilder, OpenRouterProperties properties) {
        this.webClientBuilder = webClientBuilder;
        this.apiKey = properties.getKey();
        this.apiUrl = properties.getUrl();
        
        log.info("OpenRouterClient initialized");
        log.info("  URL: {}", this.apiUrl);
        log.info("  API Key present: {}", StringUtils.hasText(this.apiKey));
        log.info("  API Key length: {}", this.apiKey != null ? this.apiKey.length() : 0);
        
        if (!StringUtils.hasText(this.apiKey)) {
            log.error("CRITICAL: OpenRouter API key is empty or not configured!");
            log.error("  - Check OPENROUTER_API_KEY environment variable is set");
            log.error("  - Check openrouter.api.key property is configured");
        }
    }

    public OpenRouterResult generate(String model, String prompt) {
        log.debug("Generating response for model: {}", model);
        return callOpenRouter(apiUrl, resolveOpenRouterModel(model), prompt, false);
    }

    private String resolveOpenRouterModel(String model) {
        if (model == null || model.isBlank()) {
            return model;
        }

        String normalized = model.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "llama-3-70b-instruct", "llama-3-70b-code", "llama-3-70b-devops", "llama-3-70b" -> "llama-3-70b-instruct";
            case "llama-3-8b-sql", "llama-3-8b" -> "llama-3-8b";
            case "mistral-7b-instruct", "mistral-7b-devops", "mistral-7b" -> "mistral-7b";
            default -> normalized;
        };
    }

    private String resolveFallbackModel(String model) {
        if (model == null || model.isBlank()) {
            return model;
        }

        String normalized = model.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("llama-3-70b") || normalized.contains("llama-3-8b")) {
            return "mistral-7b";
        }
        if (normalized.contains("mistral-7b")) {
            return "llama-3-8b";
        }
        return normalized;
    }

    private OpenRouterResult callOpenRouter(String baseUrl, String model, String prompt, boolean fallbackAttempted) {
        if (!StringUtils.hasText(apiKey)) {
            log.error("Cannot call OpenRouter: API key is empty");
            throw new RuntimeException("OpenRouter API key is not configured. Set OPENROUTER_API_KEY environment variable.");
        }
        
        log.debug("Calling OpenRouter API:");
        log.debug("  URL: {}", baseUrl);
        log.debug("  Model: {}", model);
        log.debug("  API Key length: {}", apiKey.length());
        
        String authHeader = "Bearer " + apiKey;
        log.debug("  Authorization header set: {}", authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
        
        WebClient client = webClientBuilder
                .baseUrl(baseUrl)
                .build();

        try {
            String responseJson = client.post()
                    .uri("/chat/completions")
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .bodyValue(Map.of(
                            "model", model,
                            "messages", List.of(Map.of("role", "user", "content", prompt))
                    ))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseJson == null || responseJson.isBlank()) {
                throw new RuntimeException("Empty response from OpenRouter API");
            }

            ObjectMapper mapper = new ObjectMapper();
            OpenRouterResponse response = mapper.readValue(responseJson, OpenRouterResponse.class);

            if (response.getError() != null) {
                String errorMsg = response.getError().getMessage();
                log.error("OpenRouter API error response: {}", errorMsg);
                throw new RuntimeException("OpenRouter API error: " + errorMsg);
            }

            if (response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new RuntimeException("No choices in OpenRouter response");
            }

            String content = response.getChoices().get(0).getMessage().getContent();
            if (content == null || content.isBlank()) {
                throw new RuntimeException("OpenRouter response contained no content");
            }

            int totalTokens = 0;
            if (response.getUsage() != null) {
                totalTokens = response.getUsage().getPromptTokens() + response.getUsage().getCompletionTokens();
            }

            return new OpenRouterResult(content, totalTokens);
        } catch (WebClientResponseException e) {
            String body = e.getResponseBodyAsString();
            log.error("OpenRouter returned HTTP {}: {}", e.getRawStatusCode(), body);
            
            if (e.getRawStatusCode() == 401) {
                log.error("AUTHENTICATION FAILED:");
                log.error("  - API Key length: {}", apiKey.length());
                log.error("  - API Key starts with: {}", apiKey.substring(0, Math.min(10, apiKey.length())));
                log.error("  - Response: {}", body);
            }
            
            if (!fallbackAttempted && ((e.getRawStatusCode() == 400 && body != null && (body.contains("not a valid model ID") || body.contains("invalid request error")))
                    || (e.getRawStatusCode() == 404 && body != null && body.contains("No endpoints found")))) {
                String fallbackModel = resolveFallbackModel(model);
                if (!fallbackModel.equals(model)) {
                    log.warn("Retrying OpenRouter call with fallback model {} after endpoint error for {}", fallbackModel, model);
                    return callOpenRouter(baseUrl, fallbackModel, prompt, true);
                }
            }
            throw new RuntimeException("OpenRouter API returned " + e.getRawStatusCode() + ": " + body, e);
        } catch (Exception e) {
            log.error("Error calling OpenRouter API", e);
            throw new RuntimeException("Failed to call OpenRouter API: " + e.getMessage(), e);
        }
    }
}
