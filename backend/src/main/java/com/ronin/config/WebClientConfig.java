package com.ronin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.net.URI;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder(
            @Value("${http.proxyHost:}") String httpProxyHost,
            @Value("${http.proxyPort:0}") int httpProxyPort,
            @Value("${https.proxyHost:}") String httpsProxyHost,
            @Value("${https.proxyPort:0}") int httpsProxyPort,
            @Value("${http.proxyUser:}") String proxyUser,
            @Value("${http.proxyPassword:}") String proxyPassword
    ) {
        HttpClient httpClient = HttpClient.create();
        ProxySettings proxySettings = buildProxySettings(httpProxyHost, httpProxyPort, httpsProxyHost, httpsProxyPort);
        if (proxySettings != null) {
            httpClient = httpClient.proxy(proxy -> {
                proxy.type(ProxyProvider.Proxy.HTTP)
                        .host(proxySettings.host)
                        .port(proxySettings.port);

                if (StringUtils.hasText(proxyUser) && StringUtils.hasText(proxyPassword)) {
                    proxy.username(usernameSpec -> usernameSpec.setUsername(proxyUser))
                            .password(passwordSpec -> passwordSpec.setPassword(proxyPassword));
                } else if (StringUtils.hasText(proxySettings.username) && StringUtils.hasText(proxySettings.password)) {
                    proxy.username(usernameSpec -> usernameSpec.setUsername(proxySettings.username))
                            .password(passwordSpec -> passwordSpec.setPassword(proxySettings.password));
                }
            });
        }

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    private ProxySettings buildProxySettings(String httpProxyHost, int httpProxyPort, String httpsProxyHost, int httpsProxyPort) {
        if (StringUtils.hasText(httpsProxyHost)) {
            return new ProxySettings(httpsProxyHost, resolvePort(httpsProxyPort));
        }
        if (StringUtils.hasText(httpProxyHost)) {
            return new ProxySettings(httpProxyHost, resolvePort(httpProxyPort));
        }

        String envProxy = firstNonEmpty(System.getenv("HTTPS_PROXY"), System.getenv("https_proxy"),
                System.getenv("HTTP_PROXY"), System.getenv("http_proxy"));
        if (StringUtils.hasText(envProxy)) {
            return parseProxyUri(envProxy.trim());
        }

        return null;
    }

    private ProxySettings parseProxyUri(String proxyUri) {
        try {
            if (!proxyUri.contains("://")) {
                proxyUri = "http://" + proxyUri;
            }
            URI uri = URI.create(proxyUri);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 8080;
            String username = null;
            String password = null;
            if (StringUtils.hasText(uri.getUserInfo())) {
                String[] userInfoParts = uri.getUserInfo().split(":", 2);
                username = userInfoParts[0];
                if (userInfoParts.length > 1) {
                    password = userInfoParts[1];
                }
            }
            return new ProxySettings(host, port, username, password);
        } catch (Exception ex) {
            return null;
        }
    }

    private int resolvePort(int port) {
        return port > 0 ? port : 8080;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static class ProxySettings {
        final String host;
        final int port;
        final String username;
        final String password;

        ProxySettings(String host, int port) {
            this(host, port, null, null);
        }

        ProxySettings(String host, int port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }
    }
}
