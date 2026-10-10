package com.project.DomainRegistrationLive.dto.response;

import com.project.DomainRegistrationLive.dto.SnapshotModels.*;
import java.time.LocalDate;
import java.util.List;

public record DayStatsResponse(
    LocalDate date,
    long totalDomains,
    List<KeywordEntry> topKeywords,
    List<TldEntry> topTlds,
    List<RegistrarEntry> registrars,
    List<WordEntry> topPrefix,
    List<WordEntry> topSuffix,
    FormatEntry format
) {}
