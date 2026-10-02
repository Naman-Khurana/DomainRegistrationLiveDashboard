package com.project.DomainRegistrationLive.dto.projection;

import com.project.DomainRegistrationLive.dto.KeywordStat;

import java.util.List;

public record KeywordAnalyticsProjection(
        List<KeywordStat> topKeywords,
        List<KeywordStat> prefixes,
        List<KeywordStat> suffixes
) {}
