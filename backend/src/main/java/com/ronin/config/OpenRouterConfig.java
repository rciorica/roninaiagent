package com.ronin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(OpenRouterProperties.class)
@Slf4j
public class OpenRouterConfig {
}

@ConfigurationProperties(prefix = "openrouter.api")
@Slf4j
public class OpenRouterProperties {
    private String key;
    private String url = "https://openrouter.ai/api/v1";

    public OpenRouterProperties() {
        // Try to read from environment variable if not set via properties
        String envKey = System.getenv("OPENROUTER_API_KEY");
        if (StringUtils.hasText(envKey)) {
            this.key = envKey;
            log.info("OpenRouter API key loaded from OPENROUTER_API_KEY environment variable");
        } else {
            log.warn("OPENROUTER_API_KEY environment variable not found");
        }
    }

    public String getKey() {
        // Double-check environment variable on each access
        if (!StringUtils.hasText(this.key)) {
            String envKey = System.getenv("OPENROUTER_API_KEY");
            if (StringUtils.hasText(envKey)) {
                return envKey;
            }
        }
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
