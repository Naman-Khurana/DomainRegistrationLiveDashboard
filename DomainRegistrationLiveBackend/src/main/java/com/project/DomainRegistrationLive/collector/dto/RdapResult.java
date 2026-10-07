package com.project.DomainRegistrationLive.collector.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public sealed interface RdapResult {

    record Confirmed(LocalDateTime registeredAt, Integer registrarId) implements RdapResult {
    }

    record NotFound() implements RdapResult {
    }

    record RateLimited(Duration retryAfter) implements RdapResult {
    }

    record Failed(String reason) implements RdapResult {
    }
}