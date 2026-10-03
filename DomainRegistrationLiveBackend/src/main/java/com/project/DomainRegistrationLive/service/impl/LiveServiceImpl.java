package com.project.DomainRegistrationLive.service.impl;

import com.project.DomainRegistrationLive.config.SnapshotProperties;
import com.project.DomainRegistrationLive.entity.Snapshot;
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
import java.util.Optional;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveServiceImpl implements LiveService {

    private final SnapshotRepository snapshotRepository;
    private final FeedService feedService;
    private final SnapshotProperties snapshotProperties;



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

    private static String iso(Long epochMillis) {
        return epochMillis == null ? null : Instant.ofEpochMilli(epochMillis).toString();
    }

}
