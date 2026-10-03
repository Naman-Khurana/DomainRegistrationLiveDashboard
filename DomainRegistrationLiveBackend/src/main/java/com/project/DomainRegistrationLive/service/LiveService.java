package com.project.DomainRegistrationLive.service;

import com.project.DomainRegistrationLive.dto.SnapshotModels;
import com.project.DomainRegistrationLive.entity.Snapshot;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface LiveService {
    @Transactional(readOnly = true)
    Optional<SnapshotModels.SnapshotResponse> latestSnapshot();

    @Transactional(readOnly = true)
    Optional<SnapshotModels.FeedResponse> initialFeed();

    Optional<Snapshot> loadLatest();
}
