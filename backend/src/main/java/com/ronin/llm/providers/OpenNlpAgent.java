package com.ronin.llm.providers;

import com.ronin.llm.LLMAgent;
import com.ronin.llm.LLMResult;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OpenNlpAgent implements LLMAgent {

    @Override
    public String getName() {
        return "OpenNLP";
    }

    @Override
    public boolean supports(String providerName) {
        return providerName != null && providerName.trim().toLowerCase().startsWith("opennlp");
    }

    @Override
    public LLMResult generate(String providerName, String prompt) {
        String[] tokens = SimpleTokenizer.INSTANCE.tokenize(prompt);
        long wordCount = Arrays.stream(tokens).filter(token -> token.matches("[A-Za-z0-9_'-]+")).count();
        Map<String, Long> frequency = Arrays.stream(tokens)
                .map(String::toLowerCase)
                .filter(token -> token.matches("[A-Za-z0-9_'-]+"))
                .collect(Collectors.groupingBy(token -> token, Collectors.counting()));

        String topTokens = frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> entry.getKey() + "(" + entry.getValue() + ")")
                .collect(Collectors.joining(", "));

        String content = "OpenNLP analysis:\n"
                + "Total tokens: " + tokens.length + "\n"
                + "Word count: " + wordCount + "\n"
                + "Most frequent terms: " + topTokens + "\n"
                + "Original prompt excerpt: " + (prompt.length() > 500 ? prompt.substring(0, 500) + "..." : prompt);

        return new LLMResult(content, 0);
    }
}
