package com.ronin.llm;

import com.ronin.llm.dto.ImageGenerationRequest;
import com.ronin.llm.dto.ImageGenerationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Image Generation API Controller
 * 
 * Usage Examples:
 * 
 * 1. Generate image with DALL-E 3 (OpenAI):
 *    POST /llm/generate-image
 *    {
 *      "prompt": "A serene landscape with mountains and lakes at sunset",
 *      "projectId": 1,
 *      "provider": "dall-e",
 *      "size": "1024x1024",
 *      "quality": 1
 *    }
 * 
 * 2. Generate image with Stable Diffusion (OpenRouter):
 *    POST /llm/generate-image
 *    {
 *      "prompt": "A futuristic AI agent working on code",
 *      "projectId": 1,
 *      "provider": "openrouter",
 *      "model": "stabilityai/stable-diffusion-3",
 *      "size": "1024x1024",
 *      "quality": 0
 *    }
 * 
 * 3. List available providers:
 *    GET /llm/image-providers
 * 
 * Configuration (Environment Variables):
 * - OPENAI_API_KEY: For DALL-E 3 image generation
 * - OPENROUTER_API_KEY: For Stable Diffusion image generation
 */
@RestController
@RequestMapping("/llm/images")
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationExampleController {
    // This is a documentation placeholder. Actual endpoints are in LLMController
}
