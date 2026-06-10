package com.ronin.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebPageService {

    private final WebClient.Builder webClientBuilder;
    private static final Pattern URL_PATTERN = Pattern.compile("(https?://[\\w\\-\\._~:/?#\\[\\]@!$&'()*+,;=%]+)", Pattern.CASE_INSENSITIVE);
    private static final int MAX_EXTERNAL_URLS = 3;
    private static final int MAX_PAGE_TEXT_LENGTH = 7000;
    private static final Duration FETCH_TIMEOUT = Duration.ofSeconds(10);

    public List<String> extractUrls(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> urls = new LinkedHashSet<>();
        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find() && urls.size() < MAX_EXTERNAL_URLS) {
            String url = matcher.group(1);
            if (url != null && !url.isBlank()) {
                urls.add(url.trim().replaceAll("[.,;:!?]+$", ""));
            }
        }
        return new ArrayList<>(urls);
    }

    public List<FetchedPage> fetchUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return List.of();
        }

        List<FetchedPage> pages = new ArrayList<>();
        for (String url : urls.stream().distinct().limit(MAX_EXTERNAL_URLS).toList()) {
            try {
                Optional<FetchedPage> page = fetchPage(url);
                page.ifPresent(pages::add);
            } catch (Exception ex) {
                log.warn("Unable to fetch external page {}: {}", url, ex.getMessage());
            }
        }
        return pages;
    }

    private Optional<FetchedPage> fetchPage(String url) {
        try {
            WebClient client = webClientBuilder
                    .baseUrl(deriveBaseUrl(url))
                    .defaultHeader("User-Agent", "Ronin-Agent/1.0")
                    .build();

            String body = client.get()
                    .uri(derivePath(url))
                    .accept(MediaType.TEXT_HTML, MediaType.TEXT_PLAIN)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(FETCH_TIMEOUT);

            if (body == null || body.isBlank()) {
                return Optional.empty();
            }

            String text = extractText(body, url);
            if (text.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new FetchedPage(url, truncate(text, MAX_PAGE_TEXT_LENGTH)));
        } catch (Exception ex) {
            log.warn("Failed to retrieve URL {}: {}", url, ex.getMessage());
            return Optional.empty();
        }
    }

    private String deriveBaseUrl(String url) {
        int idx = url.indexOf("//");
        if (idx < 0) {
            return url;
        }
        int pathStart = url.indexOf('/', idx + 2);
        return pathStart < 0 ? url : url.substring(0, pathStart);
    }

    private String derivePath(String url) {
        int idx = url.indexOf("//");
        if (idx < 0) {
            return url;
        }
        int pathStart = url.indexOf('/', idx + 2);
        return pathStart < 0 ? "/" : url.substring(pathStart);
    }

    private String extractText(String html, String url) {
        try {
            return Jsoup.parse(html, url).text();
        } catch (Exception ex) {
            log.warn("Failed to parse HTML for {}: {}", url, ex.getMessage());
            return html.replaceAll("<[^>]+>", " ").trim();
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "\n... [truncated]";
    }
}
