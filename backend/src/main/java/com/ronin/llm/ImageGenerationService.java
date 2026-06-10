package com.ronin.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronin.auth.CurrentUserService;
import com.ronin.projects.ProjectEntity;
import com.ronin.projects.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationService {

    private final WebClient.Builder webClientBuilder;
    private final GeneratedImageRepository generatedImageRepository;
    private final ProjectService projectService;
    private final CurrentUserService currentUserService;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openrouter.api.key:}")
    private String openRouterApiKey;

    /**
     * Generate an image using DALL-E 3 via OpenAI API
     * Requires OPENAI_API_KEY environment variable
     */
    public ImageGenerationResult generateImageDallE(String prompt, String size, int quality) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key not configured. Set OPENAI_API_KEY environment variable.");
        }

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", "dall-e-3");
            requestBody.put("prompt", prompt);
            requestBody.put("n", 1);
            requestBody.put("size", size != null ? size : "1024x1024");
            requestBody.put("quality", quality > 0 ? "hd" : "standard");
            requestBody.put("response_format", "url");

            String requestJson = OBJECT_MAPPER.writeValueAsString(requestBody);

            WebClient client = webClientBuilder
                    .baseUrl("https://api.openai.com")
                    .defaultHeader("Authorization", "Bearer " + openAiApiKey)
                    .build();

            String response = client.post()
                    .uri("/v1/images/generations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from OpenAI API");
            }

            Map<String, Object> responseMap = OBJECT_MAPPER.readValue(response, Map.class);

            if (responseMap.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) responseMap.get("error");
                String errorMessage = error.get("message").toString();
                throw new RuntimeException("OpenAI API error: " + errorMessage);
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
            if (data == null || data.isEmpty()) {
                throw new RuntimeException("No image data in OpenAI response");
            }

            String imageUrl = data.get(0).get("url").toString();
            String revisedPrompt = (String) data.get(0).get("revised_prompt");

            return new ImageGenerationResult(
                    imageUrl,
                    "dall-e-3",
                    prompt,
                    revisedPrompt,
                    "url"
            );

        } catch (Exception e) {
            log.error("Failed to generate image with DALL-E 3: {}", e.getMessage(), e);
            throw new RuntimeException("Image generation failed: " + e.getMessage());
        }
    }

    /**
     * Generate an image using Stable Diffusion via OpenRouter
     * Requires OPENROUTER_API_KEY environment variable
     */
    public ImageGenerationResult generateImageStableDiffusion(String prompt, String model, String size) {
        if (openRouterApiKey == null || openRouterApiKey.isBlank()) {
            throw new IllegalStateException("OpenRouter API key not configured. Set OPENROUTER_API_KEY environment variable.");
        }

        try {
            // Parse size like "1024x1024" into height and width
            int width = 1024, height = 1024;
            if (size != null && size.contains("x")) {
                String[] parts = size.split("x");
                width = Integer.parseInt(parts[0]);
                height = Integer.parseInt(parts[1]);
            }

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("prompt", prompt);
            requestBody.put("height", height);
            requestBody.put("width", width);
            requestBody.put("num_inference_steps", 30);

            String selectedModel = model != null ? model : "stabilityai/stable-diffusion-3";

            String requestJson = OBJECT_MAPPER.writeValueAsString(requestBody);

            WebClient client = webClientBuilder
                    .baseUrl("https://openrouter.ai")
                    .defaultHeader("Authorization", "Bearer " + openRouterApiKey)
                    .defaultHeader("HTTP-Referer", "https://ronin-backend.herokuapp.com")
                    .build();

            String response = client.post()
                    .uri("/api/v1/images/generations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from OpenRouter API");
            }

            Map<String, Object> responseMap = OBJECT_MAPPER.readValue(response, Map.class);

            if (responseMap.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) responseMap.get("error");
                String errorMessage = error.get("message").toString();
                throw new RuntimeException("OpenRouter API error: " + errorMessage);
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
            if (data == null || data.isEmpty()) {
                throw new RuntimeException("No image data in OpenRouter response");
            }

            String imageUrl = data.get(0).get("url").toString();

            return new ImageGenerationResult(
                    imageUrl,
                    selectedModel,
                    prompt,
                    null,
                    "url"
            );

        } catch (Exception e) {
            log.error("Failed to generate image with Stable Diffusion: {}", e.getMessage(), e);
            throw new RuntimeException("Image generation failed: " + e.getMessage());
        }
    }

    /**
     * Generate an image using any provider (auto-select based on available API keys)
     */
    public ImageGenerationResult generateImage(String prompt, String provider, String model, String size, int quality, Long projectId) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be empty");
        }

        String selectedProvider = provider != null ? provider : determineAvailableProvider();

        ImageGenerationResult result = switch (selectedProvider.toLowerCase()) {
            case "dall-e", "openai" -> generateImageDallE(prompt, size, quality);
            // Note: OpenRouter image generation is not currently supported
            default -> throw new IllegalArgumentException("Unsupported image generation provider: " + selectedProvider + ". Only DALL-E (OpenAI) is currently supported.");
        };

        // Save generated image to database
        if (projectId != null && projectId > 0) {
            saveGeneratedImage(result, model, size, quality, projectId);
        }

        return result;
    }

    private void saveGeneratedImage(ImageGenerationResult result, String model, String size, int quality, Long projectId) {
        try {
            ProjectEntity project = projectService.getProject(projectId);
            
            GeneratedImageEntity image = new GeneratedImageEntity();
            image.setProject(project);
            image.setUser(currentUserService.get());
            image.setPrompt(result.prompt());
            image.setRevisedPrompt(result.revisedPrompt());
            image.setImageUrl(result.imageUrl());
            image.setProvider(result.provider());
            image.setModel(model);
            image.setSize(size != null ? size : "1024x1024");
            image.setFormat(result.format());
            image.setGenerationTimeMs(0L);

            generatedImageRepository.save(image);
            log.debug("Saved generated image for project {}", projectId);
        } catch (Exception e) {
            log.warn("Failed to save generated image: {}", e.getMessage());
        }
    }

    private String determineAvailableProvider() {
        // Prefer DALL-E (OpenRouter image generation endpoint is not available)
        if (openAiApiKey != null && !openAiApiKey.isBlank()) {
            return "dall-e";
        } else {
            throw new IllegalStateException("No image generation API keys configured. Set OPENAI_API_KEY for DALL-E image generation.");
        }
    }

    public record ImageGenerationResult(
            String imageUrl,
            String provider,
            String prompt,
            String revisedPrompt,
            String format
    ) {}
}
