package com.project.DomainRegistrationLive.service;

import com.project.DomainRegistrationLive.dto.SnapshotModels;
import com.project.DomainRegistrationLive.dto.projection.RisingKeywordProjection;
import com.project.DomainRegistrationLive.timer.SectionTimer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

public interface SnapshotStatsService {
    StatsPayload buildStatsPayload(LocalDateTime now, SectionTimer timer);

    long[] counter(LocalDateTime from, LocalDateTime now);

    List<KeywordEntry> topKeywords(LocalDateTime from, LocalDateTime now, long total);

    List<TldEntry> tlds(LocalDateTime from, LocalDateTime now, long total);

    List<RegistrarEntry> registrars(LocalDateTime from, LocalDateTime now);

    Map<String, List<RisingEntry>> loadRising();

    List<RisingEntry> rank(List<RisingKeywordProjection> rows,
                           ToLongFunction<RisingKeywordProjection> recentOf,
                           ToLongFunction<RisingKeywordProjection> priorOf);

    List<WordEntry> edges(boolean prefix, LocalDateTime from, LocalDateTime now);

    FormatEntry loadFormat();

    List<HourEntry> loadHourly();

    List<MoverEntry> loadMovers();

    List<RepeatEntry> loadRepeats();
}
