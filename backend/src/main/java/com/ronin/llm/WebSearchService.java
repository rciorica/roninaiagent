package com.ronin.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSearchService {

    private final WebClient.Builder webClientBuilder;
    private static final Pattern SEARCH_QUERY_PATTERN = Pattern.compile(
            "(?i)(?:search|look up|lookup|find|google|bing|web search|web lookup)(?:\\s+(?:for|about|on))?\\s+(.+)");
    private static final int MAX_SEARCH_RESULTS = 3;
    private static final Duration SEARCH_TIMEOUT = Duration.ofSeconds(10);
    private static final String SEARCH_BASE_URL = "https://html.duckduckgo.com/html/";

    public Optional<String> extractSearchQuery(String message) {
        if (message == null || message.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = SEARCH_QUERY_PATTERN.matcher(message.trim());
        if (matcher.find()) {
            String query = matcher.group(1).trim();
            if (!query.isBlank()) {
                return Optional.of(query.replaceAll("[?!.]+$", ""));
            }
        }
        return Optional.empty();
    }

    public Optional<SearchResult> search(String query) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }

        try {
            String url = SEARCH_BASE_URL + "?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            WebClient client = webClientBuilder
                    .baseUrl("https://html.duckduckgo.com")
                    .defaultHeader("User-Agent", "Ronin-Agent/1.0")
                    .build();

            String html = client.get()
                    .uri(uriBuilder -> uriBuilder.path("/html/").queryParam("q", query).build())
                    .accept(MediaType.TEXT_HTML)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(SEARCH_TIMEOUT);

            if (html == null || html.isBlank()) {
                return Optional.empty();
            }

            List<SearchSnippet> snippets = extractSnippets(html);
            if (snippets.isEmpty()) {
                return Optional.of(new SearchResult(query, List.of()));
            }

            return Optional.of(new SearchResult(query, snippets));
        } catch (Exception ex) {
            log.warn("Web search failed for query '{}': {}", query, ex.getMessage());
            return Optional.empty();
        }
    }

    private List<SearchSnippet> extractSnippets(String html) {
        Document doc = Jsoup.parse(html);
        Elements resultElements = doc.select(".result__a");
        List<SearchSnippet> snippets = new ArrayList<>();

        if (!resultElements.isEmpty()) {
            for (Element link : resultElements) {
                if (snippets.size() >= MAX_SEARCH_RESULTS) {
                    break;
                }
                String title = link.text();
                String href = link.absUrl("href");
                if (href.isBlank()) {
                    href = link.attr("href").trim();
                }
                String description = "";
                Element parent = link.parent();
                if (parent != null) {
                    Element snippet = parent.selectFirst(".result__snippet");
                    if (snippet != null) {
                        description = snippet.text();
                    }
                }
                snippets.add(new SearchSnippet(title, href, description));
            }
        }

        if (snippets.isEmpty()) {
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                if (snippets.size() >= MAX_SEARCH_RESULTS) {
                    break;
                }
                String href = link.absUrl("href");
                if (href.isBlank() || href.contains("duckduckgo.com")) {
                    continue;
                }
                String title = link.text();
                if (title.isBlank()) {
                    continue;
                }
                snippets.add(new SearchSnippet(title, href, ""));
            }
        }

        return snippets;
    }
}
