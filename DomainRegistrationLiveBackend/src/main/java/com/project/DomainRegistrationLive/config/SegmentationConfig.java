package com.project.DomainRegistrationLive.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SegmentationProperties.class)
public class SegmentationConfig {
}
