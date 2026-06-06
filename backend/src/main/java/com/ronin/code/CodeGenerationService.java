package com.ronin.code;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for generating and validating code according to Ronin's code quality rules:
 * - Keep code simple
 * - Keep code clean
 * - Test code after chunks of steps are done
 * - Ensure integration frontend-backend works after chunks of steps are done
 * - Think analytical and goal oriented
 * - Use design patterns and SOLID principles
 * - Run tests
 */
@Slf4j
@Service
public class CodeGenerationService {

    private static final String CODE_GENERATION_SYSTEM_PROMPT = """
        You are a code generation assistant for Ronin, an AI-powered project building platform.
        
        When generating code, ALWAYS follow these rules:
        1. Keep code simple - avoid unnecessary complexity; favor readability over cleverness
        2. Keep code clean - follow naming conventions, consistent formatting, proper documentation
        3. Think analytical and goal oriented - understand requirements first, implement with clear purpose
        4. Use design patterns and SOLID principles - apply proven patterns for maintainability
        5. Each code block should be testable
        6. Ensure generated code can integrate seamlessly with existing components
        7. Include unit tests for critical functionality
        
        After generating code, review it against these rules before responding.
        """;

    /**
     * Get system prompt that enforces code generation rules
     */
    public String getSystemPrompt() {
        return CODE_GENERATION_SYSTEM_PROMPT;
    }

    /**
     * Validate generated code follows quality rules
     * @param code The generated code to validate
     * @param language Programming language (java, typescript, python, etc.)
     * @return CodeValidationResult with issues and suggestions
     */
    public CodeValidationResult validateCode(String code, String language) {
        log.debug("Validating code quality for {}", language);
        
        CodeValidationResult result = new CodeValidationResult();
        result.setLanguage(language);
        
        // Rule 1: Complexity check (basic - method length, nesting depth)
        checkComplexity(code, language, result);
        
        // Rule 2: Code cleanliness check (naming, formatting)
        checkCleanliness(code, language, result);
        
        // Rule 3: Design patterns check
        checkDesignPatterns(code, language, result);
        
        // Rule 4: Documentation check
        checkDocumentation(code, language, result);
        
        // Rule 5: Testability check
        checkTestability(code, language, result);
        
        return result;
    }

    private void checkComplexity(String code, String language, CodeValidationResult result) {
        // Check method lengths - flag methods > 30 lines
        String[] lines = code.split("\n");
        if (lines.length > 30) {
            result.addWarning("Code block is lengthy (" + lines.length + " lines). Consider breaking into smaller methods.");
        }
        
        // Check nesting depth - flag if > 4 levels
        int maxNesting = getMaxNestingDepth(code, language);
        if (maxNesting > 4) {
            result.addWarning("Nesting depth is high (" + maxNesting + " levels). Consider refactoring for simplicity.");
        }
    }

    private void checkCleanliness(String code, String language, CodeValidationResult result) {
        // Check for missing documentation/comments on public methods
        if (language.equals("java") && code.contains("public ")) {
            if (!code.contains("/**") && !code.contains("//")) {
                result.addWarning("Public methods should include documentation/comments.");
            }
        }
        
        if (language.equals("typescript") && code.contains("export ")) {
            if (!code.contains("/**") && !code.contains("//")) {
                result.addWarning("Exported functions should include documentation/comments.");
            }
        }
    }

    private void checkDesignPatterns(String code, String language, CodeValidationResult result) {
        // Check for SOLID principle violations
        if (language.equals("java")) {
            // Check for God Classes (too many methods/fields)
            int methodCount = code.split("(public|private)\\s+\\w+\\s+\\w+\\(").length;
            if (methodCount > 15) {
                result.addSuggestion("Class has many methods (" + methodCount + "). Consider applying Single Responsibility Principle.");
            }
        }
    }

    private void checkDocumentation(String code, String language, CodeValidationResult result) {
        int docLines = code.split("(//|/\\*\\*|\\*\\/)").length;
        if (docLines < 2) {
            result.addSuggestion("Consider adding more documentation/comments for maintainability.");
        }
    }

    private void checkTestability(String code, String language, CodeValidationResult result) {
        // Check for static methods, hard-coded dependencies
        if (code.contains("static ")) {
            result.addSuggestion("Static methods reduce testability. Consider using dependency injection.");
        }
        
        if (code.toLowerCase().contains("new ") && code.contains("database") || code.contains("http")) {
            result.addSuggestion("Consider injecting external dependencies for better testability.");
        }
    }

    private int getMaxNestingDepth(String code, String language) {
        int maxDepth = 0;
        int currentDepth = 0;
        
        for (char c : code.toCharArray()) {
            if (c == '{') currentDepth++;
            if (c == '}') currentDepth--;
            maxDepth = Math.max(maxDepth, currentDepth);
        }
        
        return maxDepth;
    }

    /**
     * Code validation result containing issues and suggestions
     */
    public static class CodeValidationResult {
        private String language;
        private boolean valid = true;
        private java.util.List<String> errors = new java.util.ArrayList<>();
        private java.util.List<String> warnings = new java.util.ArrayList<>();
        private java.util.List<String> suggestions = new java.util.ArrayList<>();

        public void addError(String error) {
            errors.add(error);
            valid = false;
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public void addSuggestion(String suggestion) {
            suggestions.add(suggestion);
        }

        // Getters
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public boolean isValid() { return valid; }
        public java.util.List<String> getErrors() { return errors; }
        public java.util.List<String> getWarnings() { return warnings; }
        public java.util.List<String> getSuggestions() { return suggestions; }
    }
}
