package com.ronin.llm;

import com.ronin.llm.dto.ChatRequest;
import com.ronin.llm.dto.ChatResponse;
import com.ronin.llm.dto.ImageGenerationRequest;
import com.ronin.llm.dto.ImageGenerationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/llm")
@RequiredArgsConstructor
public class LLMController {

    private final LLMService llmService;
    private final LLMAgentRegistry llmAgentRegistry;
    private final ImageGenerationService imageGenerationService;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest req) {
        return llmService.chat(req);
    }

    @GetMapping("/agents")
    public List<String> getLLMAgents() {
        return llmAgentRegistry.getAgentNames();
    }

    @PostMapping("/generate-image")
    public ImageGenerationResponse generateImage(@RequestBody ImageGenerationRequest req) {
        long startTime = System.currentTimeMillis();
        
        try {
            ImageGenerationService.ImageGenerationResult result = imageGenerationService.generateImage(
                    req.prompt(),
                    req.provider(),
                    req.model(),
                    req.size() != null ? req.size() : "1024x1024",
                    req.quality(),
                    req.projectId()
            );

            long generationTime = System.currentTimeMillis() - startTime;

            return new ImageGenerationResponse(
                    result.imageUrl(),
                    result.provider(),
                    result.prompt(),
                    result.revisedPrompt(),
                    result.format(),
                    generationTime
            );
        } catch (Exception e) {
            long generationTime = System.currentTimeMillis() - startTime;
            throw new RuntimeException("Image generation failed: " + e.getMessage(), e);
        }
    }

    @GetMapping("/image-providers")
    public Map<String, Object> getImageProviders() {
        return Map.of(
                "providers", List.of(
                        Map.of(
                                "name", "pollinations-ai",
                                "displayName", "Pollinations.ai (Flux Pro)",
                                "description", "Free high-quality image generation - no API key required",
                                "models", List.of("flux-pro"),
                                "sizes", List.of("1024x1024", "768x768", "1280x720", "1920x1080")
                        ),
                        Map.of(
                                "name", "dall-e",
                                "displayName", "DALL-E 3 (OpenAI)",
                                "description", "Premium image generation (requires OPENAI_API_KEY)",
                                "models", List.of("dall-e-3"),
                                "sizes", List.of("1024x1024", "1792x1024", "1024x1792")
                        )
                )
        );
    }
}
