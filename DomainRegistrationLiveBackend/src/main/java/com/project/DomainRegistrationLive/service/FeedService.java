package com.project.DomainRegistrationLive.service;

import com.project.DomainRegistrationLive.dto.SnapshotModels.*;
import com.project.DomainRegistrationLive.entity.Domain;

import java.util.List;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

public interface FeedService {
    List<FeedEntry> read(long afterId, long toId);

    //for new session
    List<FeedEntry> latest(long toId, int limit);

    List<FeedEntry> toEntries(List<Domain> newestFirst);
}
