package com.ronin.llm;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WebSearchTool {
    private final WebSearchService webSearchService;

    @Tool("Searches the live internet for up-to-date facts, news, and information.")
    public String searchTheWeb(String query) {
        Optional<SearchResult> result = webSearchService.search(query);
        if (result.isPresent()) {
            SearchResult searchResult = result.get();
            StringBuilder output = new StringBuilder();
            output.append("Search Results for '").append(query).append("':\n");
            
            if (searchResult.snippets() != null && !searchResult.snippets().isEmpty()) {
                for (SearchSnippet snippet : searchResult.snippets()) {
                    output.append("- Title: ").append(snippet.title()).append("\n");
                    output.append("  URL: ").append(snippet.url()).append("\n");
                    if (snippet.description() != null && !snippet.description().isEmpty()) {
                        output.append("  Description: ").append(snippet.description()).append("\n");
                    }
                }
            } else {
                output.append("No results found.");
            }
            return output.toString();
        }
        return "Search failed for query: " + query;
    }
}
