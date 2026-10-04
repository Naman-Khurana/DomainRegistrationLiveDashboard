package com.project.DomainRegistrationLive.dto.response;

import com.project.DomainRegistrationLive.dto.SnapshotModels;

import java.util.List;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

public record BlockResponse(
    List<KeywordEntry> topKeywords,
    List<TldEntry> topTlds
) {
}
