package com.ronin.llm;

import java.util.List;

public record SearchResult(String query, List<SearchSnippet> snippets) {
}
