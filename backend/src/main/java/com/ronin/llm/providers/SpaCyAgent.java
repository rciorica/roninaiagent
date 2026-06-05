package com.ronin.llm.providers;

import com.ronin.llm.LLMAgent;
import com.ronin.llm.LLMResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SpaCyAgent implements LLMAgent {

    @Value("${spacy.python.executable:python}")
    private String pythonExecutable;

    @Value("${spacy.model:en_core_web_sm}")
    private String model;

    @Override
    public String getName() {
        return "spaCy";
    }

    @Override
    public boolean supports(String providerName) {
        return providerName != null && providerName.trim().toLowerCase().startsWith("spacy");
    }

    @Override
    public LLMResult generate(String providerName, String prompt) {
        try {
            String[] command = {
                    pythonExecutable,
                    "-c",
                    "import sys, json; import spacy; nlp = spacy.load('" + model + "'); doc = nlp(sys.stdin.read());\n"
                            + "entities = [{'text': ent.text, 'label': ent.label_} for ent in doc.ents];\n"
                            + "print(json.dumps({'sentences': [sent.text for sent in doc.sents],'entities': entities}))"
            };
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            process.getOutputStream().write(prompt.getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().close();

            String output = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("spaCy process exited with code " + exitCode + ": " + output);
            }
            return new LLMResult("spaCy analysis result:\n" + output, 0);
        } catch (Exception e) {
            log.error("spaCy integration failed", e);
            throw new RuntimeException("spaCy integration failed: " + e.getMessage(), e);
        }
    }
}
