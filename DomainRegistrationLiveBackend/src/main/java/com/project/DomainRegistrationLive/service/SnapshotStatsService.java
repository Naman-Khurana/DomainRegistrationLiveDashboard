package com.project.DomainRegistrationLive.service;

import com.project.DomainRegistrationLive.dto.SnapshotModels;
import com.project.DomainRegistrationLive.timer.SectionTimer;

import java.time.LocalDateTime;

public interface SnapshotStatsService {
    SnapshotModels.StatsPayload buildStatsPayload(LocalDateTime now, SectionTimer timer);
}
