package com.project.DomainRegistrationLive.service;

import com.project.DomainRegistrationLive.dto.response.DailySummaryResponse;
import com.project.DomainRegistrationLive.dto.response.SearchResponse;
import com.project.DomainRegistrationLive.entity.Snapshot;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

public interface LiveService {
    @Transactional(readOnly = true)
    Optional<SnapshotResponse> latestSnapshot();

    @Transactional(readOnly = true)
    Optional<FeedResponse> initialFeed();

    Optional<Snapshot> loadLatest();

    SearchResponse search(String keyword, Integer windowStart);

    DailySummaryResponse daily(LocalDate date);
}
