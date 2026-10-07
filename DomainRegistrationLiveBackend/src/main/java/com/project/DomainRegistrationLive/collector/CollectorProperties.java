package com.project.DomainRegistrationLive.collector;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@ConfigurationProperties(prefix = "collector")
public record CollectorProperties (
        @DefaultValue("false") boolean enabled,
        @DefaultValue("4") int rdapWorkers, // rdap threads: also the maximum number of lookups in flight
        @DefaultValue("5000") int candidateQueueSize,
        @DefaultValue("1000") int resultQueueSize,
        // A domain is ingested only if its registry creation time is newer than this.
        @DefaultValue("60m") Duration maxAge,
        @DefaultValue("5.0") double rdapMaxPerHostPerSecond,
        @DefaultValue("50") int batchSize,
        @DefaultValue("2s") Duration flushInterval,
        // How long a domain is remembered, so it is looked up only once.
        @DefaultValue("24h") Duration seenTtl,
        @DefaultValue("300000") long seenMaxSize,
        @DefaultValue("8s") Duration httpTimeout,
        @DefaultValue("30s") Duration statsInterval,
        @DefaultValue("https://data.iana.org/rdap/dns.json") String bootstrapUrl,
        // locally run certstream-server-go.
        @DefaultValue("ws://localhost:8081/") String certstreamUrl,
        @DefaultValue("60s") Duration silenceTimeout
) {

    public CollectorProperties {
        if (rdapWorkers < 1 || rdapWorkers > 16) {
            throw new IllegalArgumentException("collector.rdap-workers must be between 1 and 16");
        }
        if (candidateQueueSize < 1 || resultQueueSize < 1 || batchSize < 1) {
            throw new IllegalArgumentException("collector queue and batch sizes must be at least 1");
        }
        if (rdapMaxPerHostPerSecond <= 0) {
            throw new IllegalArgumentException("collector.rdap-max-per-host-per-second must be positive");
        }
    }
}
