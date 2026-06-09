package com.ronin.llm.providers;

public class OpenRouterResult {
    private String content;
    private int tokensUsed;
    
    public OpenRouterResult(String content, int tokensUsed) {
        this.content = content;
        this.tokensUsed = tokensUsed;
    }
    
    public String getContent() {
        return content;
    }
    
    public int getTokensUsed() {
        return tokensUsed;
    }
}
