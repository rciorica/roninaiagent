package com.ronin.llm;

import com.ronin.llm.LLMAgentRegistry;
import com.ronin.llm.dto.ChatRequest;
import com.ronin.llm.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/llm")
@RequiredArgsConstructor
public class LLMController {

    private final LLMService llmService;
    private final LLMAgentRegistry llmAgentRegistry;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest req) {
        return llmService.chat(req);
    }

    @GetMapping("/agents")
    public List<String> getLLMAgents() {
        return llmAgentRegistry.getAgentNames();
    }
}
