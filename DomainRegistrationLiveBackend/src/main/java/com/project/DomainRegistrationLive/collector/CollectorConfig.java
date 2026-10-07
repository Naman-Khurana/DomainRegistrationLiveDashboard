package com.project.DomainRegistrationLive.collector;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CollectorProperties.class)
public class CollectorConfig {
}
