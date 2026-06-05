package com.ronin.llm;

public interface LLMAgent {
    String getName();

    boolean supports(String providerName);

    LLMResult generate(String providerName, String prompt);
}
