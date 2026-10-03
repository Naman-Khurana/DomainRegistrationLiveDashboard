package com.project.DomainRegistrationLive.service.impl;

import com.project.DomainRegistrationLive.config.SnapshotProperties;
import com.project.DomainRegistrationLive.entity.Domain;
import com.project.DomainRegistrationLive.entity.DomainKeyword;
import com.project.DomainRegistrationLive.entity.Snapshot;
import com.project.DomainRegistrationLive.enums.DomainStatus;
import com.project.DomainRegistrationLive.repository.DomainRepository;
import com.project.DomainRegistrationLive.repository.SnapshotRepository;
import com.project.DomainRegistrationLive.service.FeedService;
import com.project.DomainRegistrationLive.service.LiveService;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveServiceImpl implements LiveService {

    private final SnapshotRepository snapshotRepository;
    private final FeedService feedService;
    private final SnapshotProperties snapshotProperties;
    private final DomainRepository domainRepository;



    //return empty until the first snapshot has been built
    @Transactional(readOnly = true)
    @Override
    public Optional<SnapshotResponse> latestSnapshot() {
        log.info("snapshot request served");
        return loadLatest().map(s -> {
            StatsPayload stats = s.getStats();
            return new SnapshotResponse(
                    s.getId(),
                   stats.builtAt(),
                    stats.lastCycleAt(),
                    System.currentTimeMillis(),
                    stats.now(),
                    stats.today(),
                    s.getFeed());
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
    public List<FeedEntry> search(String keyword, Integer windowStart) {
        String trimmedKeyword = String.join("", keyword.split(" "));
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime windowBegin = currentTime.minusMinutes(windowStart + 60);
        LocalDateTime windowEnd = currentTime.minusMinutes(windowStart);

        List<Domain> searchResult = new ArrayList<>();
        if(!trimmedKeyword.startsWith(".")) {
            searchResult = domainRepository.findBySldContainingIgnoreCaseAndRegisteredAtGreaterThanEqualAndRegisteredAtLessThanAndStatus(
                    trimmedKeyword, windowBegin, windowEnd, DomainStatus.PARSED
            ).orElse(null);
        }
        else{
            //remove .
            trimmedKeyword = trimmedKeyword.substring(1);
            searchResult = domainRepository.findByTldContainingIgnoreCaseAndRegisteredAtGreaterThanEqualAndRegisteredAtLessThanAndStatus(
                    trimmedKeyword, windowBegin, windowEnd, DomainStatus.PARSED
            );
        }

        if(searchResult == null || searchResult.isEmpty()){
            return List.of();
        }

        return feedService.toEntries(searchResult);
    }

    private static String iso(Long epochMillis) {
        return epochMillis == null ? null : Instant.ofEpochMilli(epochMillis).toString();
    }

}
