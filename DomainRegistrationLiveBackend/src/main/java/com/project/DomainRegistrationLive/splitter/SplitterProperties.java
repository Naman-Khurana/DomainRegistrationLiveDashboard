package com.project.DomainRegistrationLive.splitter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "splitter")
public record SplitterProperties(
        String url,
        Duration connectTimeout,
        Duration readTimeout
) {

    public SplitterProperties{
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("splitter.url must be set");
        }
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(2);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(10);
        }
    }
}
