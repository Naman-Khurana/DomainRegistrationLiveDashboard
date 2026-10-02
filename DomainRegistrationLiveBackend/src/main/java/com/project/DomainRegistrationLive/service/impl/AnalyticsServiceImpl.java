//package com.project.DomainRegistrationLive.service.impl;
//
//import com.project.DomainRegistrationLive.dto.*;
//import com.project.DomainRegistrationLive.dto.projection.FeedDomainProjection;
//import com.project.DomainRegistrationLive.dto.projection.KeywordAnalyticsProjection;
//import com.project.DomainRegistrationLive.dto.projection.KeywordStatsProjection;
//import com.project.DomainRegistrationLive.dto.projection.RisingKeywordProjection;
//import com.project.DomainRegistrationLive.dto.response.RisingKeyword;
//import com.project.DomainRegistrationLive.enums.DomainStatus;
//import com.project.DomainRegistrationLive.repository.DomainKeywordRepository;
//import com.project.DomainRegistrationLive.repository.DomainRepository;
//import com.project.DomainRegistrationLive.service.AnalyticsService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Comparator;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class AnalyticsServiceImpl implements AnalyticsService {
//
//    private final DomainRepository domainRepository;
//    private final DomainKeywordRepository domainKeywordRepository;
//
//    @Override
//    public DomainStatsProjection getDomainStats() {
//
//        LocalDateTime now = LocalDateTime.now();
//
//        LocalDateTime h60Start = now.minusHours(1);
//        LocalDateTime m10Start = now.minusMinutes(10);
//        LocalDateTime m1Start = now.minusMinutes(1);
//
//        return domainRepository.getDomainStats(
//                h60Start,
//                m10Start,
//                m1Start,
//                now,
//                DomainStatus.PARSED
//        );
//    }
//
//    @Override
//    public List<TldCountProjection> getTldCounts() {
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime windowStart = now.minusHours(1);
//
//        return domainRepository.getTldCounts(
//                windowStart,
//                now,
//                DomainStatus.PARSED
//        );
//    }
//
//
//    @Override
//    public List<RegistrarCountProjection> getRegistrarCounts() {
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime windowStart = now.minusHours(1);
//
//        return domainRepository.getRegistrarCounts(
//                windowStart,
//                now,
//                DomainStatus.PARSED
//        );
//    }
//
//    @Override
//    public List<KeywordCountProjection> getKeywordCounts() {
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime windowStart = now.minusHours(1);
//
//        return domainKeywordRepository.getKeywordCounts(
//                windowStart,
//                now
//        );
//    }
//
//
//    @Override
//    public List<PrefixCountProjection> getPrefixCounts() {
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime windowStart = now.minusHours(1);
//
//        return domainKeywordRepository.getPrefixCounts(
//                windowStart,
//                now
//        );
//    }
//
//
//    @Override
//    public List<SuffixCountProjection> getSuffixCounts() {
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime windowStart = now.minusHours(1);
//
//        return domainKeywordRepository.getSuffixCounts(
//                windowStart,
//                now
//        );
//    }
//
//    @Override
//    public DomainFormatProjection getDomainFormat() {
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime windowStart = now.minusHours(1);
//
//        return domainRepository.getDomainFormat(
//                windowStart,
//                now,
//                DomainStatus.PARSED
//        );
//    }
//
//
//    public List<RepeatedSldProjection> getRepeatedSlds() {
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime windowStart = now.minusHours(1);
//
//        List<SldTldProjection> rows = domainRepository.getSldTlds(
//                windowStart,
//                now,
//                DomainStatus.PARSED
//        );
//
//        return rows.stream()
//                .collect(Collectors.groupingBy(
//                        SldTldProjection::sld,
//                        LinkedHashMap::new,
//                        Collectors.mapping(
//                                SldTldProjection::tld,
//                                Collectors.toList()
//                        )
//                ))
//                .entrySet()
//                .stream()
//                .filter(entry -> entry.getValue().size() > 1)
//                .map(entry -> new RepeatedSldProjection(
//                        entry.getKey(),
//                        (long) entry.getValue().size(),
//                        entry.getValue()
//                ))
//                .toList();
//    }
//
//    @Override
//    public KeywordAnalyticsProjection getKeywordAnalytics() {
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime windowStart = now.minusHours(1);
//
//        List<KeywordStatsProjection> stats = null;
////                domainKeywordRepository.getKeywordStats(windowStart, now);
//
//        int topK = 10;
//
//        List<KeywordStat> topKeywords = stats.stream()
//                .limit(topK)
//                .map(this::toKeywordStat)
//                .toList();
//
//        List<KeywordStat> prefixes = stats.stream()
//                .filter(stat -> stat.prefixCount() > 0)
//                .sorted(Comparator
//                        .comparing(KeywordStatsProjection::prefixCount)
//                        .reversed()
//                        .thenComparing(KeywordStatsProjection::keyword))
//                .limit(topK)
//                .map(this::toKeywordStat)
//                .toList();
//
//        List<KeywordStat> suffixes = stats.stream()
//                .filter(stat -> stat.suffixCount() > 0)
//                .sorted(Comparator
//                        .comparing(KeywordStatsProjection::suffixCount)
//                        .reversed()
//                        .thenComparing(KeywordStatsProjection::keyword))
//                .limit(topK)
//                .map(this::toKeywordStat)
//                .toList();
//
//        return new KeywordAnalyticsProjection(
//                topKeywords,
//                prefixes,
//                suffixes
//        );
//    }
//
//    private KeywordStat toKeywordStat(KeywordStatsProjection stat) {
//        return new KeywordStat(
//                stat.keyword(),
//                stat.count(),
//                stat.prefixCount(),
//                stat.suffixCount()
//        );
//    }
//
//    @Override
//    public RisingKeywordAnalytics getRisingKeywords() {
//
//        LocalDateTime now = LocalDateTime.now();
//
//        LocalDateTime recent15mStart = now.minusMinutes(15);
//        LocalDateTime prior15mStart = now.minusMinutes(30);
//
//        LocalDateTime recent1hStart = now.minusHours(1);
//        LocalDateTime prior1hStart = now.minusHours(2);
//
//        LocalDateTime recent3hStart = now.minusHours(3);
//        LocalDateTime prior3hStart = now.minusHours(6);
//
//        List<RisingKeywordProjection> stats =
//                domainKeywordRepository.getRisingKeywordStats(
//                        recent15mStart,
//                        prior15mStart,
//                        recent1hStart,
//                        prior1hStart,
//                        recent3hStart,
//                        prior3hStart,
//                        now
//                );
//
//        return new RisingKeywordAnalytics(
//                getTopRising15m(stats),
//                getTopRising1h(stats),
//                getTopRising3h(stats)
//        );
//    }
//
//    public List<RisingKeyword> getTopRising15m(
//            List<RisingKeywordProjection> stats) {
//
//        return stats.stream()
//                .filter(s -> s.recent15m() > 0)
//                .map(s -> new RisingKeyword(
//                        s.keyword(),
//                        s.recent15m(),
//                        s.prior15m(),
//                        calculateLift(s.recent15m(), s.prior15m())
//                ))
//                .sorted(Comparator
//                        .comparing(RisingKeyword::lift)
//                        .reversed()
//                        .thenComparing(RisingKeyword::word))
//                .limit(10)
//                .toList();
//    }
//
//
//    private List<RisingKeyword> getTopRising1h(
//            List<RisingKeywordProjection> stats) {
//
//        return stats.stream()
//                .filter(s -> s.recent1h() > 0)
//                .map(s -> new RisingKeyword(
//                        s.keyword(),
//                        s.recent1h(),
//                        s.prior1h(),
//                        calculateLift(s.recent1h(), s.prior1h())
//                ))
//                .sorted(Comparator
//                        .comparing(RisingKeyword::lift)
//                        .reversed()
//                        .thenComparing(RisingKeyword::word))
//                .limit(10)
//                .toList();
//    }
//
//    private List<RisingKeyword> getTopRising3h(
//            List<RisingKeywordProjection> stats) {
//
//        return stats.stream()
//                .filter(s -> s.recent3h() > 0)
//                .map(s -> new RisingKeyword(
//                        s.keyword(),
//                        s.recent3h(),
//                        s.prior3h(),
//                        calculateLift(s.recent3h(), s.prior3h())
//                ))
//                .sorted(Comparator
//                        .comparing(RisingKeyword::lift)
//                        .reversed()
//                        .thenComparing(RisingKeyword::word))
//                .limit(10)
//                .toList();
//    }
//
//    private double calculateLift(Long recent, Long prior) {
//
//        if (prior == 0) {
//            return recent;
//        }
//
//        return (double) recent / prior;
//    }
//
//    @Override
//    public FeedResponse getFeed(LocalDateTime since) {
//
//        if (since == null) {
//            since = LocalDateTime.now().minusYears(100);
//        }
//
//        List<FeedDomainProjection> domains =
//                domainRepository.getFeed(
//                        since,
//                        DomainStatus.PARSED.name()
//                );
//
//        LocalDateTime latestRegisteredAt = domains.stream()
//                .map(FeedDomainProjection::registeredAt)
//                .max(LocalDateTime::compareTo)
//                .orElse(since);
//
//        return new FeedResponse(
//                domains,
//                latestRegisteredAt
//        );
//    }
//}
