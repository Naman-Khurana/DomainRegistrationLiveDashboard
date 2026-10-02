package com.project.DomainRegistrationLive.service;

import com.project.DomainRegistrationLive.dto.*;
import com.project.DomainRegistrationLive.dto.projection.KeywordAnalyticsProjection;
import com.project.DomainRegistrationLive.dto.projection.RisingKeywordProjection;
import com.project.DomainRegistrationLive.dto.response.RisingKeyword;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsService {
    DomainStatsProjection getDomainStats();

    List<TldCountProjection> getTldCounts();

    List<RegistrarCountProjection> getRegistrarCounts();

    List<KeywordCountProjection> getKeywordCounts();

    List<PrefixCountProjection> getPrefixCounts();

    List<SuffixCountProjection> getSuffixCounts();

    DomainFormatProjection getDomainFormat();

    List<RepeatedSldProjection> getRepeatedSlds();

    KeywordAnalyticsProjection getKeywordAnalytics();

    RisingKeywordAnalytics getRisingKeywords();

    List<RisingKeyword> getTopRising15m(
            List<RisingKeywordProjection> stats);

    FeedResponse getFeed(LocalDateTime since);
}
