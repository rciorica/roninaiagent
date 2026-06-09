package com.ronin.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LLMAgentRegistry {

    private final List<LLMAgent> agents;

    public LLMAgent resolve(String providerName) {
        return agents.stream()
                .filter(agent -> agent.supports(providerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No LLM agent available for provider: " + providerName));
    }

    public List<String> getAgentNames() {
        return agents.stream().map(LLMAgent::getName).toList();
    }
}
