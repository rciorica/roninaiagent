package com.ronin.llm.providers;

import com.ronin.llm.LLMAgent;
import com.ronin.llm.LLMResult;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "stanford.corenlp.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class StanfordCoreNLPAgent implements LLMAgent {

    private StanfordCoreNLP pipeline;

    public StanfordCoreNLPAgent() {
        try {
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
            props.setProperty("ner.applyNumericClassifiers", "false");
            props.setProperty("ner.useSUTime", "false");
            this.pipeline = new StanfordCoreNLP(props);
        } catch (Exception e) {
            log.error("Failed to initialize Stanford CoreNLP pipeline", e);
            this.pipeline = null;
        }
    }

    @Override
    public String getName() {
        return "Stanford CoreNLP";
    }

    @Override
    public boolean supports(String providerName) {
        return providerName != null && providerName.trim().toLowerCase().startsWith("stanfordcorenlp");
    }

    @Override
    public LLMResult generate(String providerName, String prompt) {
        if (pipeline == null) {
            throw new IllegalStateException("Stanford CoreNLP pipeline is unavailable. Ensure the model dependency is present and the pipeline initialized correctly.");
        }

        Annotation document = new Annotation(prompt);
        pipeline.annotate(document);

        List<String> sentences = document.get(CoreAnnotations.SentencesAnnotation.class).stream()
                .map(sentence -> sentence.get(CoreAnnotations.TextAnnotation.class))
                .collect(Collectors.toList());

        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);
        String namedEntities = tokens.stream()
                .filter(token -> token.get(CoreAnnotations.NamedEntityTagAnnotation.class) != null)
                .map(token -> token.word() + ":" + token.get(CoreAnnotations.NamedEntityTagAnnotation.class))
                .distinct()
                .collect(Collectors.joining(", "));

        String summary = "Stanford CoreNLP analysis:\n"
                + "Sentences: " + sentences.size() + "\n"
                + "Tokens: " + tokens.size() + "\n"
                + "Named Entities: " + (namedEntities.isBlank() ? "none" : namedEntities) + "\n"
                + "First sentence: " + (sentences.isEmpty() ? "none" : sentences.get(0));

        return new LLMResult(summary, 0);
    }
}
