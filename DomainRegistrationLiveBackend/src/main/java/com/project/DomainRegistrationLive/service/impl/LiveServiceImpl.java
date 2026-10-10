package com.project.DomainRegistrationLive.service.impl;

import com.project.DomainRegistrationLive.cache.SnapshotCache;
import com.project.DomainRegistrationLive.config.SnapshotProperties;
import com.project.DomainRegistrationLive.dto.response.BlockResponse;
import com.project.DomainRegistrationLive.dto.response.DailySummaryResponse;
import com.project.DomainRegistrationLive.dto.response.DayStatsResponse;
import com.project.DomainRegistrationLive.dto.response.SearchResponse;
import com.project.DomainRegistrationLive.entity.Domain;
import com.project.DomainRegistrationLive.entity.Snapshot;
import com.project.DomainRegistrationLive.enums.DomainStatus;
import com.project.DomainRegistrationLive.repository.DomainRepository;
import com.project.DomainRegistrationLive.repository.SnapshotRepository;
import com.project.DomainRegistrationLive.service.FeedService;
import com.project.DomainRegistrationLive.service.LiveService;
//import jakarta.transaction.Transactional;
import com.project.DomainRegistrationLive.service.SnapshotStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveServiceImpl implements LiveService {

    private final SnapshotRepository snapshotRepository;
    private final FeedService feedService;
    private final SnapshotProperties snapshotProperties;
    private final DomainRepository domainRepository;
    private final SnapshotCache snapshotCache;
    private final SnapshotStatsService snapshotStatsService;




    //return empty until the first snapshot has been built
    @Transactional(readOnly = true)
    @Override
    public Optional<SnapshotResponse> latestSnapshot() {
        log.info("snapshot request served");

        Optional<SnapshotResponse> cachedSnapshot = snapshotCache.get(SnapshotCache.LATEST_SNAPSHOT_KEY);

        if(cachedSnapshot.isPresent()){
            return cachedSnapshot;
        }

        log.info("Redis miss for snapshot, falling back to DB");

        return loadLatest().map(s -> {
            StatsPayload stats = s.getStats();
            SnapshotResponse response = new SnapshotResponse(
                    s.getId(),
                   stats.builtAt(),
                    stats.lastCycleAt(),
                    System.currentTimeMillis(),
                    stats.now(),
                    stats.today(),
                    s.getFeed());

            snapshotCache.put(
                    SnapshotCache.LATEST_SNAPSHOT_KEY,
                    response
            );
            log.info("snapshot cached");

            return response;
        });
    }

    // return empty until the first snapshot has been built
    @Transactional(readOnly = true)
    @Override
    public Optional<FeedResponse> initialFeed() {
        return loadLatest().map(s -> new FeedResponse(
                s.getId(),
                feedService.latest(s.getToSeq(), snapshotProperties.feedPreload())));
    }

    // The one place that reads the latest snapshot.  Put a cache (e.g. Redis)
    @Override
    public Optional<Snapshot> loadLatest() {
        return snapshotRepository.findTopByOrderByIdDesc();
    }

    @Override
    public SearchResponse search(String keyword, Integer windowStart) {
        String trimmedKeyword = String.join("", keyword.split(" "));
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime windowBegin = currentTime.minusMinutes(windowStart + 60);
        LocalDateTime windowEnd = currentTime.minusMinutes(windowStart);

        boolean isTld = trimmedKeyword.startsWith(".");

        List<Domain> searchResult = new ArrayList<>();
        if(!isTld) {
            searchResult = domainRepository.findTop200BySldContainingIgnoreCaseAndRegisteredAtGreaterThanEqualAndRegisteredAtLessThanAndStatusOrderByIdDesc(
                    trimmedKeyword, windowBegin, windowEnd, DomainStatus.PARSED
            ).orElse(null);
        }
        else{
            //remove .
            trimmedKeyword = trimmedKeyword.substring(1);
            searchResult = domainRepository.findTop200ByTldContainingIgnoreCaseAndRegisteredAtGreaterThanEqualAndRegisteredAtLessThanAndStatusOrderByIdDesc(
                    trimmedKeyword, windowBegin, windowEnd, DomainStatus.PARSED
            );
        }

        String responseKeyword = (isTld ? "." : "") + trimmedKeyword;

        if(searchResult == null || searchResult.isEmpty()){
            return new SearchResponse(responseKeyword, List.of());
        }

        List<FeedEntry> results = feedService.toEntries(searchResult);

        return new SearchResponse(responseKeyword, results);
    }

    @Override
    public DailySummaryResponse daily(LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        long total = snapshotStatsService.counter(from, to)[0];
        List<RegistrarEntry> registrarEntries = snapshotStatsService.registrars(from, to, total);
        return new DailySummaryResponse(date, registrarEntries);
    }

    @Override
    public DayStatsResponse dayStats(LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        long[] counter = snapshotStatsService.counter(from, to);
        long total = counter[0];

        List<KeywordEntry> topKeywords = snapshotStatsService.topKeywords(from, to, total);
        List<TldEntry> topTlds = snapshotStatsService.tlds(from, to, total);
        List<RegistrarEntry> registrars = snapshotStatsService.registrars(from, to, total);
        
        List<WordEntry> prefix = snapshotStatsService.edges(true, from, to);
        List<WordEntry> suffix = snapshotStatsService.edges(false, from, to);
        FormatEntry format = snapshotStatsService.loadFormat(from, to);

        return new DayStatsResponse(date, total, topKeywords, topTlds, registrars, prefix, suffix, format);
    }

    @Override
    public BlockResponse block(int i) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime from = currentTime.minusMinutes(60 + i);
        LocalDateTime to = currentTime.minusMinutes(i);

        List<KeywordEntry> topKeywords = snapshotStatsService.topKeywords(from, to, snapshotProperties.topKeywords());
        List<TldEntry> topTlds = snapshotStatsService.tlds(from, to, snapshotProperties.topTlds());

        return new BlockResponse(topKeywords, topTlds);
    }


    private static String iso(Long epochMillis) {
        return epochMillis == null ? null : Instant.ofEpochMilli(epochMillis).toString();
    }

}
