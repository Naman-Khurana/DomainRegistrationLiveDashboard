package com.project.DomainRegistrationLive.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "segmentation")
public record SegmentationProperties (
        @DefaultValue("100") int batchSize,
        @DefaultValue("5") int maxAttempts,
        @DefaultValue("1s") Duration idleSleep,
        @DefaultValue("5s") Duration errorBackoff
){
    public SegmentationProperties {
        if (batchSize < 1) {
            throw new IllegalArgumentException("segmentation.batch-size must be at least 1");
        }
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("segmentation.max-attempts must be at least 1");
        }
    }


}
