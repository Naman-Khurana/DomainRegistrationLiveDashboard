package com.project.DomainRegistrationLive.dto.response;

import java.util.List;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

public record SearchResponse(
        String keyword,
        List<FeedEntry> entries

) {
}
