package com.project.DomainRegistrationLive.dto;

import com.project.DomainRegistrationLive.dto.projection.FeedDomainProjection;

import java.time.LocalDateTime;
import java.util.List;

public record FeedResponse(
        List<FeedDomainProjection> items,
        LocalDateTime latestRegisteredAt
) {}
