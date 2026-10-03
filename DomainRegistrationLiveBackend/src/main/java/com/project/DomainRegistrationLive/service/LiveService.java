package com.project.DomainRegistrationLive.service;

import com.project.DomainRegistrationLive.dto.SnapshotModels;
import com.project.DomainRegistrationLive.entity.Snapshot;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

public interface LiveService {
    @Transactional(readOnly = true)
    Optional<SnapshotResponse> latestSnapshot();

    @Transactional(readOnly = true)
    Optional<FeedResponse> initialFeed();

    Optional<Snapshot> loadLatest();

    List<FeedEntry> search(String keyword, Integer windowStart);
}
