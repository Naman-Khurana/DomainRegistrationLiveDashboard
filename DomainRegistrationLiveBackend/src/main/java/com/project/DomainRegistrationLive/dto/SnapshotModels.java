package com.project.DomainRegistrationLive.dto;

import java.util.List;
import java.util.Map;

public final class SnapshotModels {

    private SnapshotModels() {
    }

    public record KeywordEntry(String word, long count, long pfx, long sfx, boolean stop) {
    }

    public record TldEntry(String tld, long count, double share) {
    }

    public record RegistrarEntry(Long registrarId, String registrar, long count) {
    }

    public record RisingEntry(String word, long recent, long prior, double lift) {
    }

    public record WordEntry(String word, long count) {
    }

    public record FormatEntry(long total, long multiword, long oneword, long numeric, long hyphen, long short5) {
    }

    public record RepeatEntry(String sld, List<String> words, int tlds, List<String> tldList) {
    }

    /** hr is an ISO hour start in UTC, e.g. 2026-10-01T16:00:00Z */
    public record HourEntry(String hr, long count) {
    }

    public record MoverEntry(String word, long today, double baseline, double lift) {
    }

    /** seq is the domain id, t is registeredAt in epoch milliseconds (UTC). */
    public record FeedEntry(long seq, String domain, String tld,
                            Long registrarId, String registrar, long t) {
    }

    public record NowStats(long h60, long m10, long m1, double perMin,
                           KeywordEntry topKeyword,
                           List<KeywordEntry> topKeywords,
                           List<TldEntry> tlds,
                           List<RegistrarEntry> registrars,
                           Map<String, List<RisingEntry>> risingKeywords,   // keys: "15m", "1h", "3h"
                           List<WordEntry> prefixes,
                           List<WordEntry> suffixes,
                           FormatEntry format,
//                           List<RepeatEntry> repeats,
                           List<HourEntry> hourly) {
    }

    public record TodayStats(List<MoverEntry> movers) {
    }

    /** Stored in Snapshot.stats. */
    public record StatsPayload(long builtAt, Long lastCycleAt, NowStats now, TodayStats today) {
    }
}
