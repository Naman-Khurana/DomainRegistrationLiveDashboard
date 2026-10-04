package com.project.DomainRegistrationLive.worker;

import com.project.DomainRegistrationLive.cache.SnapshotCache;
import com.project.DomainRegistrationLive.config.SnapshotProperties;
import com.project.DomainRegistrationLive.dto.SnapshotModels;
import com.project.DomainRegistrationLive.entity.Snapshot;
import com.project.DomainRegistrationLive.enums.DomainStatus;
import com.project.DomainRegistrationLive.enums.SnapshotBuildStage;
import com.project.DomainRegistrationLive.repository.DomainRepository;
import com.project.DomainRegistrationLive.repository.SnapshotRepository;
import com.project.DomainRegistrationLive.service.FeedService;
import com.project.DomainRegistrationLive.service.SnapshotStatsService;
import com.project.DomainRegistrationLive.timer.SectionTimer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class SnapshotBuilder {

    private final SnapshotRepository snapshotRepository;
    private final DomainRepository domainRepository;
    private final SnapshotProperties snapshotProperties;
    private final FeedService feedService;
    private final SnapshotStatsService snapshotStatsService;
    private final SnapshotCache snapshotCache;


    @Scheduled(fixedRateString = "${snapshot.interval:10s}", initialDelayString = "${snapshot.initial-delay:3s}")
    public void build(){
        SectionTimer timer = new SectionTimer();
        try {
            buildOnce(timer);
            if (timer.totalMillis() > snapshotProperties.slowBuildWarnMs()) {
                log.warn("slow snapshot build: {}", timer.summary());
            } else {
                log.info("snapshot built: {}", timer.summary());   // switch to debug once tuned
            }
        }
        catch (Exception e){
            log.error("snapshot build failed after {} ms: {}",timer.totalMillis(), timer.summary(), e);
        }
    }

    @Transactional
    private void buildOnce(SectionTimer timer){
        LocalDateTime now = LocalDateTime.now();

        Snapshot previous = timer.time(SnapshotBuildStage.PREVIOUS_SNAPSHOT.name(), () -> snapshotRepository.findTopByOrderByIdDesc().orElse(null));

        Long maxId = timer.time("maxFeedId", () -> domainRepository.findMaxIdByStatus(DomainStatus.PARSED));
        long toSeq = maxId == null ? 0L : maxId;
        long fromSeq = previous == null ? toSeq : Math.min(previous.getToSeq(), toSeq);

        List<FeedEntry> feed = timer.time(SnapshotBuildStage.FEED.name(), () -> feedService.read(fromSeq, toSeq));

        StatsPayload payload = snapshotStatsService.buildStatsPayload(now, timer);

        Snapshot saved = timer.time("save", () -> snapshotRepository.save(
                Snapshot.builder()
                        .fromSeq(fromSeq)
                        .toSeq(toSeq)
                        .stats(payload)
                        .feed(feed)
                        .build()));

        SnapshotResponse response = new SnapshotResponse(
                saved.getId(),
                payload.builtAt(),
                payload.lastCycleAt(),
                System.currentTimeMillis(),
                payload.now(),
                payload.today(),
                saved.getFeed());

        snapshotCache.put(SnapshotCache.LATEST_SNAPSHOT_KEY, response);
        log.info("snapshot cached");

        timer.run("prune", () -> snapshotRepository.deleteOlderThan(saved.getId() - snapshotProperties.snapshotsToKeep()));
    }
}
