package com.project.DomainRegistrationLive.cache;

import com.project.DomainRegistrationLive.dto.SnapshotModels;
import com.project.DomainRegistrationLive.entity.Snapshot;

import java.util.Optional;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

public interface SnapshotCache {
    public static final String LATEST_SNAPSHOT_KEY = "latest";

    Optional<SnapshotResponse> get(String keyId);

    void put(String keyId, SnapshotResponse snapshotCache);

    void evict(String keyId);

}