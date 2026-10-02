package com.project.DomainRegistrationLive.dto.projection;

import java.time.LocalDateTime;

public record FeedDomainProjection(
        Long id,
        String domain,
        String tld,
        Integer registrarId,
        LocalDateTime registeredAt
) {}