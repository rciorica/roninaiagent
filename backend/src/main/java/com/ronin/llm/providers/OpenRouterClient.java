package com.ronin.llm.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class OpenRouterClient {

    private final WebClient.Builder webClientBuilder;
    private final String apiKey;
    private final String apiUrl;

    public OpenRouterClient(WebClient.Builder webClientBuilder,
                            @Value("${OPENROUTER_API_KEY:#{null}}") String envApiKey,
                            @Value("${openrouter.api.key:#{null}}") String propApiKey,
                            @Value("${openrouter.api.url:https://openrouter.ai/api/v1}") String apiUrl) {
        this.webClientBuilder = webClientBuilder;
        
        // Try multiple sources in order of preference
        String finalKey = null;
        String source = "none";
        
        // 1. Try environment variable directly via Spring
        if (StringUtils.hasText(envApiKey)) {
            finalKey = envApiKey;
            source = "OPENROUTER_API_KEY env var (Spring)";
        }
        // 2. Try property binding
        else if (StringUtils.hasText(propApiKey)) {
            finalKey = propApiKey;
            source = "openrouter.api.key property";
        }
        // 3. Fallback to System.getenv()
        else {
            String sysEnvKey = System.getenv("OPENROUTER_API_KEY");
            if (StringUtils.hasText(sysEnvKey)) {
                finalKey = sysEnvKey;
                source = "System.getenv(OPENROUTER_API_KEY)";
            }
        }
        
        log.info("===== OpenRouterClient Initialization =====");
        log.info("API URL: {}", apiUrl);
        log.info("API Key Source: {}", source);
        log.info("API Key Present: {}", StringUtils.hasText(finalKey));
        if (StringUtils.hasText(finalKey)) {
            log.info("API Key Length: {} chars", finalKey.length());
            log.info("API Key Full Value: '{}'", finalKey);
            log.info("API Key Hex: {}", bytesToHex(finalKey.getBytes(StandardCharsets.UTF_8)));
            log.info("API Key Base64: {}", Base64.getEncoder().encodeToString(finalKey.getBytes(StandardCharsets.UTF_8)));
            
            // Check for common issues
            if (finalKey.contains("[") || finalKey.contains("]")) {
                log.warn("WARNING: API Key contains bracket characters - may be a placeholder!");
            }
            if (finalKey.equals("[REDACTED]")) {
                log.error("CRITICAL: API Key is the literal string '[REDACTED]' - not a real API key!");
            }
            if (finalKey.length() < 20) {
                log.warn("WARNING: API Key is very short ({} chars) - typical OpenRouter keys are 40+ chars", finalKey.length());
            }
        }
        log.info("=========================================");
        
        if (!StringUtils.hasText(finalKey)) {
            log.error("CRITICAL: OpenRouter API key not found in any source!");
            log.error("  - Not found in OPENROUTER_API_KEY environment variable");
            log.error("  - Not found in openrouter.api.key property");
            log.error("  - Not found in System.getenv()");
            log.error("Please set OPENROUTER_API_KEY environment variable");
        }
        
        this.apiKey = finalKey != null ? finalKey : "";
        this.apiUrl = apiUrl;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
            log.error("CRITICAL: Cannot call OpenRouter - API key is empty!");
            log.error("Ensure OPENROUTER_API_KEY environment variable is set on Heroku:");
            log.error("  heroku config:set OPENROUTER_API_KEY=sk-or-... --app ronin-backend");
            throw new RuntimeException("OpenRouter API key not configured");
        }
        
        log.debug("Calling OpenRouter API");
        log.debug("  URL: {}", baseUrl);
        log.debug("  Model: {}", model);
        log.debug("  API Key Length: {}", apiKey.length());
        log.debug("  API Key Full: '{}'", apiKey);
        log.debug("  Authorization Header: Bearer {}", apiKey.substring(0, Math.min(20, apiKey.length())) + "...");
        
        String authHeader = "Bearer " + apiKey;
        
        WebClient client = webClientBuilder
                .baseUrl(baseUrl)
                .build();

        try {
            log.debug("Sending POST request to /chat/completions with Authorization header");
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
                log.error("OpenRouter API returned error: {}", errorMsg);
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

            log.info("OpenRouter call successful: {} tokens used", totalTokens);
            return new OpenRouterResult(content, totalTokens);
        } catch (WebClientResponseException e) {
            String body = e.getResponseBodyAsString();
            log.error("OpenRouter API returned HTTP {}", e.getRawStatusCode());
            log.error("Response body: {}", body);
            
            if (e.getRawStatusCode() == 401) {
                log.error("========== AUTHENTICATION FAILURE (401) ==========");
                log.error("API Key Details:");
                log.error("  Length: {} chars", apiKey.length());
                log.error("  Full Value: '{}'", apiKey);
                log.error("  Hex: {}", bytesToHex(apiKey.getBytes(StandardCharsets.UTF_8)));
                log.error("  Starts with: {}", apiKey.substring(0, Math.min(10, apiKey.length())));
                log.error("");
                log.error("Possible causes:");
                log.error("  1. API Key is invalid or expired");
                log.error("  2. API Key doesn't have required permissions");
                log.error("  3. API Key is malformed (should start with 'sk-or-')");
                log.error("  4. API Key is set to placeholder value like '[REDACTED]'");
                log.error("");
                log.error("OpenRouter Response: {}", body);
                log.error("================================================");
            }
            
            if (!fallbackAttempted && ((e.getRawStatusCode() == 400 && body != null && (body.contains("not a valid model ID") || body.contains("invalid request error")))
                    || (e.getRawStatusCode() == 404 && body != null && body.contains("No endpoints found")))) {
                String fallbackModel = resolveFallbackModel(model);
                if (!fallbackModel.equals(model)) {
                    log.warn("Retrying with fallback model {}", fallbackModel);
                    return callOpenRouter(baseUrl, fallbackModel, prompt, true);
                }
            }
            throw new RuntimeException("OpenRouter API returned " + e.getRawStatusCode() + ": " + body, e);
        } catch (Exception e) {
            log.error("Error calling OpenRouter API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call OpenRouter API: " + e.getMessage(), e);
        }
    }
}
