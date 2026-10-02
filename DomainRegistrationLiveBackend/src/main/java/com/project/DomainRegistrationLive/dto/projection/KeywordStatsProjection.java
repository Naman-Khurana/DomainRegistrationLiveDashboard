package com.project.DomainRegistrationLive.dto.projection;

public record KeywordStatsProjection(
        String keyword,
        Long count,
        Long prefixCount,
        Long suffixCount
) {}
