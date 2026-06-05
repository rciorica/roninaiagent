package com.ronin.llm.providers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronin.llm.LLMAgent;
import com.ronin.llm.LLMResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HuggingFaceTransformersAgent implements LLMAgent {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${huggingface.api.key:}")
    private String apiKey;

    @Value("${huggingface.api.url:https://api-inference.huggingface.co/models}")
    private String apiUrl;

    @Override
    public String getName() {
        return "HuggingFace Transformers";
    }

    @Override
    public boolean supports(String providerName) {
        return providerName != null && providerName.trim().toLowerCase().startsWith("huggingface");
    }

    @Override
    public LLMResult generate(String providerName, String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Hugging Face API key is not configured");
        }

        String modelId = resolveModelId(providerName);
        String modelPath = URLEncoder.encode(modelId, StandardCharsets.UTF_8);

        WebClient client = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        try {
            String response = client.post()
                    .uri("/" + modelPath)
                    .bodyValue(Map.of("inputs", prompt, "options", Map.of("wait_for_model", true)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                throw new RuntimeException("Empty response from Hugging Face Inference API");
            }

            if (response.trim().startsWith("[")) {
                List<Map<String, Object>> output = objectMapper.readValue(response, new TypeReference<>() {});
                String generated = output.stream()
                        .map(item -> item.getOrDefault("generated_text", "").toString())
                        .reduce("", String::concat);
                return new LLMResult(generated, 0);
            }

            Map<String, Object> output = objectMapper.readValue(response, new TypeReference<>() {});
            if (output.containsKey("generated_text")) {
                return new LLMResult(output.get("generated_text").toString(), 0);
            }
            if (output.containsKey("error")) {
                throw new RuntimeException("Hugging Face error: " + output.get("error"));
            }

            return new LLMResult(output.toString(), 0);
        } catch (WebClientResponseException e) {
            String body = e.getResponseBodyAsString();
            log.error("Hugging Face returned HTTP {}: {}", e.getRawStatusCode(), body);
            throw new RuntimeException("Hugging Face API returned " + e.getRawStatusCode() + ": " + body, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Hugging Face API: " + e.getMessage(), e);
        }
    }

    private String resolveModelId(String providerName) {
        String normalized = providerName.trim();
        if (normalized.toLowerCase().startsWith("huggingface-")) {
            return normalized.substring("huggingface-".length()).trim();
        }
        return normalized;
    }
}
