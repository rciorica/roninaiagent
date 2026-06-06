package com.ronin.llm;

import com.ronin.auth.CurrentUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronin.llm.dto.ChatRequest;
import com.ronin.llm.dto.ChatResponse;
import com.ronin.llm.LLMProviderEntity;
import com.ronin.llm.LLMAgentRegistry;
import com.ronin.llm.LLMResult;
import com.ronin.projects.ProjectEntity;
import com.ronin.projects.ProjectMessageEntity;
import com.ronin.projects.ProjectMessageRepository;
import com.ronin.projects.ProjectService;
import com.ronin.users.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMService {

    private final ModelSelector modelSelector;
    private final TokenTracker tokenTracker;
    private final ProjectService projectService;
    private final ProjectMessageRepository messageRepository;
    private final CurrentUserService currentUserService;
    private final LLMAgentRegistry llmAgentRegistry;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("\\[[\\s\\S]*?\\]");

    public ChatResponse chat(ChatRequest req) {
        ProjectEntity project = projectService.getProject(req.projectId());
        UserEntity user = project.getUser();

        saveChatMessage(project, "USER", null, req.message());

        ModelSelectionResult sel = modelSelector.select(user, project.getPhase());
        LLMProviderEntity chosen = sel.chosen();
        LLMProviderEntity preferred = sel.preferred();

        log.debug("Calling LLM provider: {} for project: {}", chosen.getName(), req.projectId());

        // Build a prompt that includes project files and uploaded attachments so the LLM can operate on the provided folder
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are an AI assistant that may modify the user's project files when requested.\n");
        promptBuilder.append("If the user asks you to change, create, or delete files, respond with only a valid JSON array. Do not include any extra text outside the JSON array.\n");
        promptBuilder.append("Each item must include: path, action (replace/create/delete), and content when applicable.\n");
        promptBuilder.append("Example:\n");
        promptBuilder.append("[\n");
        promptBuilder.append("  {\"path\": \"src/App.tsx\", \"action\": \"replace\", \"content\": \"...new file contents...\"},\n");
        promptBuilder.append("  {\"path\": \"README.md\", \"action\": \"delete\"}\n");
        promptBuilder.append("]\n");
        promptBuilder.append("If the user is only asking for advice or no file changes are required, answer naturally and do not return JSON.\n\n");
        promptBuilder.append("Context: the following project files and uploaded attachments are available to use.\n\n");

        try {
            // include artifact files (generated or saved project files)
            var artifactFiles = projectService.getProjectFiles(project.getId());
            for (var f : artifactFiles) {
                promptBuilder.append("FILE: ").append(f.getFilePath()).append('\n');
                String content = f.getContent() == null ? "" : f.getContent();
                if (content.length() > 8000) {
                    promptBuilder.append(content, 0, 8000).append("\n...[truncated]\n\n");
                } else {
                    promptBuilder.append(content).append("\n\n");
                }
            }

            // include recent message attachments (uploaded folders/files)
            List<com.ronin.projects.ProjectMessageEntity> messages = messageRepository.findByProjectIdOrderByCreatedAtAsc(project.getId());
            for (var m : messages) {
                if (m.getAttachments() == null || m.getAttachments().isEmpty()) continue;
                for (var a : m.getAttachments()) {
                    String name = a.getFileName();
                    String ct = a.getContentType() == null ? "" : a.getContentType().toLowerCase();
                    promptBuilder.append("ATTACHMENT: ").append(name).append(" (content-type: ").append(ct).append(")\n");
                    try {
                        byte[] bytes = a.getContent();
                        if (bytes == null) {
                            promptBuilder.append("(empty)\n\n");
                            continue;
                        }
                        boolean isText = ct.startsWith("text") || ct.contains("json") || ct.contains("xml") || ct.contains("javascript") || ct.contains("html") || ct.contains("css");
                        if (isText) {
                            String txt = new String(bytes, StandardCharsets.UTF_8);
                            if (txt.length() > 8000) {
                                promptBuilder.append(txt, 0, 8000).append("\n...[truncated]\n\n");
                            } else {
                                promptBuilder.append(txt).append("\n\n");
                            }
                        } else {
                            promptBuilder.append("(binary file omitted from prompt)\n\n");
                        }
                    } catch (Exception ex) {
                        promptBuilder.append("(failed to read attachment)\n\n");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to include project files/attachments in prompt: {}", e.getMessage());
        }

        // append user request at the end so the LLM knows what to perform
        promptBuilder.append("USER REQUEST:\n").append(req.message());

        LLMAgent agent = llmAgentRegistry.resolve(chosen.getName());
        LLMResult llmResult = agent.generate(chosen.getName(), promptBuilder.toString());

        String llmOutput = llmResult.content();
        int tokensUsed = llmResult.tokensUsed();

        log.debug("LLM returned {} tokens", tokensUsed);

        boolean editsApplied = false;
        int appliedEditCount = 0;
        try {
            List<LLMEditInstruction> edits = parseEditsFromResponse(llmOutput);
            if (!edits.isEmpty()) {
                appliedEditCount = applyEdits(project.getId(), edits);
                editsApplied = appliedEditCount > 0;
                if (editsApplied) {
                    llmOutput = llmOutput + "\n\n[Applied " + appliedEditCount + " project file change(s).]";
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse or apply LLM edits: {}", e.getMessage());
        }

        tokenTracker.recordUsage(user, chosen, tokensUsed);
        saveChatMessage(project, "ASSISTANT", chosen.getName(), llmOutput);

        return new ChatResponse(
                llmOutput,
                chosen.getName(),
                sel.switched(),
                preferred.getName(),
                editsApplied
        );
    }
    private List<LLMEditInstruction> parseEditsFromResponse(String llmOutput) {
        Matcher matcher = JSON_ARRAY_PATTERN.matcher(llmOutput);
        if (!matcher.find()) {
            return List.of();
        }
        String jsonPayload = matcher.group();
        try {
            return OBJECT_MAPPER.readValue(jsonPayload, new TypeReference<List<LLMEditInstruction>>() {});
        } catch (JsonProcessingException e) {
            log.debug("Unable to parse edit JSON from LLM response: {}", e.getMessage());
            return List.of();
        }
    }

    private int applyEdits(Long projectId, List<LLMEditInstruction> edits) {
        int appliedCount = 0;
        for (LLMEditInstruction edit : edits) {
            String path = Optional.ofNullable(edit.path()).orElse("").replace("\\", "/");
            if (path.isBlank()) {
                continue;
            }
            String action = Optional.ofNullable(edit.action()).orElse("replace").trim().toLowerCase();
            switch (action) {
                case "replace", "update", "create", "set" -> {
                    projectService.saveProjectFile(projectId, path, Optional.ofNullable(edit.content()).orElse(""));
                    appliedCount++;
                }
                case "delete" -> {
                    projectService.deleteProjectFile(projectId, path);
                    appliedCount++;
                }
                default -> log.warn("Skipping unsupported edit action: {} for path {}", action, path);
            }
        }
        return appliedCount;
    }

    private void saveChatMessage(ProjectEntity project, String sender, String modelUsed, String message) {
        ProjectMessageEntity projectMessage = new ProjectMessageEntity();
        projectMessage.setProject(project);
        projectMessage.setUser(currentUserService.get());
        projectMessage.setSender(sender);
        projectMessage.setModelUsed(modelUsed);
        projectMessage.setMessage(message);
        messageRepository.save(projectMessage);
    }

    private int estimateTokens(String prompt, String completion) {
        int words = (prompt.split("\\s+").length + completion.split("\\s+").length);
        return Math.max(1, words * 2); // rough heuristic
    }
}
// TODO If you later want true “previousModel → newModel” detection, you can:

// TODO store “last used provider” per project/user

// TODO or let ModelSelector return a richer object like ModelSelectionResult(previous, chosen, switched).