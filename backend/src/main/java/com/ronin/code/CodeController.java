package com.ronin.code;

import com.ronin.code.CodeGenerationService.CodeValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeGenerationService codeGenerationService;

    /**
     * Validate generated code against Ronin's code quality rules
     */
    @PostMapping("/validate")
    public ResponseEntity<CodeValidationResult> validateCode(
            @PathVariable Long projectId,
            @RequestBody CodeValidationRequest request) {
        
        log.info("Validating code for project {} in language {}", projectId, request.getLanguage());
        
        CodeValidationResult result = codeGenerationService.validateCode(
                request.getCode(),
                request.getLanguage()
        );
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get code generation system prompt that enforces rules
     */
    @GetMapping("/generation-prompt")
    public ResponseEntity<CodeGenerationPromptResponse> getCodeGenerationPrompt() {
        return ResponseEntity.ok(new CodeGenerationPromptResponse(
                codeGenerationService.getSystemPrompt()
        ));
    }

    @PostMapping("/format")
    public ResponseEntity<CodeFormatResponse> formatCode(
            @PathVariable Long projectId,
            @RequestBody CodeFormatRequest request) {
        
        log.info("Formatting code for project {}", projectId);
        
        // TODO: Implement code formatting using language-specific formatters
        // For now, return the code as-is with validation
        CodeValidationResult validation = codeGenerationService.validateCode(
                request.getCode(),
                request.getLanguage()
        );
        
        return ResponseEntity.ok(new CodeFormatResponse(
                request.getCode(),
                validation
        ));
    }

    // DTOs
    @lombok.Data
    public static class CodeValidationRequest {
        private String code;
        private String language;
    }

    @lombok.Data
    public static class CodeFormatRequest {
        private String code;
        private String language;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CodeFormatResponse {
        private String formattedCode;
        private CodeValidationResult validation;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CodeGenerationPromptResponse {
        private String systemPrompt;
    }
}
