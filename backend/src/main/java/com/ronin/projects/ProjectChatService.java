package com.ronin.projects;

import com.ronin.code.CodeGenerationService;
import com.ronin.code.CodeGenerationService.CodeValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing chat messages with code generation capabilities
 * Applies Ronin's code quality rules to all generated code
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectChatService {

    private final ProjectMessageRepository messageRepository;
    private final CodeGenerationService codeGenerationService;

    /**
     * Process a chat message and validate any code content
     * @param projectId The project ID
     * @param message The chat message text
     * @return The saved message entity
     */
    public ProjectMessageEntity processMessage(Long projectId, ProjectMessageEntity message) {
        log.debug("Processing message for project {}: {}", projectId, message.getMessage());
        
        // Check if message contains code blocks
        if (message.getMessage().contains("```")) {
            validateCodeInMessage(message);
        }
        
        return messageRepository.save(message);
    }

    /**
     * Validate code blocks found in a chat message
     * Extracts code blocks and validates them against Ronin's rules
     */
    private void validateCodeInMessage(ProjectMessageEntity message) {
        String content = message.getMessage();
        String[] parts = content.split("```");
        
        for (int i = 1; i < parts.length; i += 2) {
            String codeBlock = parts[i];
            String language = extractLanguage(codeBlock);
            String code = extractCode(codeBlock);
            
            if (!language.isEmpty() && !code.isEmpty()) {
                CodeValidationResult result = codeGenerationService.validateCode(code, language);
                
                if (!result.isValid()) {
                    log.warn("Generated code has validation errors: {}", result.getErrors());
                    message.setValidationIssues(formatValidationIssues(result));
                } else if (!result.getWarnings().isEmpty() || !result.getSuggestions().isEmpty()) {
                    log.info("Generated code has suggestions: warnings={}, suggestions={}", 
                            result.getWarnings().size(), result.getSuggestions().size());
                    message.setValidationIssues(formatValidationIssues(result));
                }
            }
        }
    }

    private String extractLanguage(String codeBlock) {
        String[] lines = codeBlock.split("\n");
        if (lines.length > 0) {
            return lines[0].trim();
        }
        return "";
    }

    private String extractCode(String codeBlock) {
        String[] lines = codeBlock.split("\n");
        if (lines.length > 1) {
            return String.join("\n", java.util.Arrays.copyOfRange(lines, 1, lines.length));
        }
        return "";
    }

    private String formatValidationIssues(CodeValidationResult result) {
        StringBuilder sb = new StringBuilder();
        
        if (!result.getErrors().isEmpty()) {
            sb.append("❌ ERRORS:\n");
            result.getErrors().forEach(e -> sb.append("  - ").append(e).append("\n"));
        }
        
        if (!result.getWarnings().isEmpty()) {
            sb.append("⚠️ WARNINGS:\n");
            result.getWarnings().forEach(w -> sb.append("  - ").append(w).append("\n"));
        }
        
        if (!result.getSuggestions().isEmpty()) {
            sb.append("💡 SUGGESTIONS:\n");
            result.getSuggestions().forEach(s -> sb.append("  - ").append(s).append("\n"));
        }
        
        return sb.toString();
    }
}
