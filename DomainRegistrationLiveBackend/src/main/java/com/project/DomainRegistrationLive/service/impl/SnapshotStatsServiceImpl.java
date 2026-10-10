package com.project.DomainRegistrationLive.service.impl;

import com.project.DomainRegistrationLive.config.SnapshotProperties;
import com.project.DomainRegistrationLive.dto.projection.KeywordStatsProjection;
import com.project.DomainRegistrationLive.dto.projection.RisingKeywordProjection;
import com.project.DomainRegistrationLive.enums.SnapshotBuildStage;
import com.project.DomainRegistrationLive.repository.DomainKeywordRepository;
import com.project.DomainRegistrationLive.repository.DomainRepository;
import com.project.DomainRegistrationLive.service.FeedService;
import com.project.DomainRegistrationLive.service.KeywordRules;
import com.project.DomainRegistrationLive.service.RegistrarDictionary;
import com.project.DomainRegistrationLive.service.SnapshotStatsService;
import com.project.DomainRegistrationLive.timer.SectionTimer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.ToLongFunction;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;
import static com.project.DomainRegistrationLive.enums.DomainStatus.PARSED;

@Service
@Slf4j
@RequiredArgsConstructor
public class SnapshotStatsServiceImpl implements SnapshotStatsService {

    private final DomainRepository domainRepository;
    private final DomainKeywordRepository domainKeywordRepository;
    private final SnapshotProperties snapshotProperties;
    private final FeedService feedService;
    private final RegistrarDictionary registrarDictionary;

    private static final int OVERFETCH = 5;
    private static final int MIN_REPEAT_TLDS = 2;
    private static final int REPEATS_LIMIT = 100;
    private static final int REPEATS_TLD_LIST = 8;


    @Override
    public StatsPayload buildStatsPayload(LocalDateTime now, SectionTimer timer){

        LocalDateTime from = now.minusHours(1);

        long[] windowCounters = timer.time(SnapshotBuildStage.COUNTER.name(), () -> counter(from,now));
        long total = windowCounters[0];

        List<KeywordEntry> topKeywords = timer.time(SnapshotBuildStage.TOP_KEYWORDS.name(),
                () -> topKeywords(from, now, snapshotProperties.topKeywords()));

        KeywordEntry topKeyword = topKeywords.stream().findFirst().orElse(null);

        List<TldEntry> tlds = timer.time(SnapshotBuildStage.TLDS.name(), () -> tlds(from, now, total));

        List<RegistrarEntry> registrars = timer.time(SnapshotBuildStage.REGISTRAR.name(), () -> registrars(from, now, total));

        Map<String,List<RisingEntry>> rising = timer.time(SnapshotBuildStage.RISING.name(), () -> loadRising());

        List<WordEntry> prefix = timer.time(SnapshotBuildStage.PREFIX.name(), () -> edges(true, from, now));
        List<WordEntry> suffix = timer.time(SnapshotBuildStage.SUFFIX.name(), () -> edges(false, from, now));


        FormatEntry format = timer.time(SnapshotBuildStage.FORMAT.name(), () -> loadFormat(from, now));

        List<HourEntry> hourly = timer.time(SnapshotBuildStage.FORMAT.name(), () -> loadHourly());

        List<MoverEntry> movers = timer.time(SnapshotBuildStage.MOVERS.name(), () -> loadMovers());

        List<RepeatEntry> repeatSldsEntries = timer.time(SnapshotBuildStage.REPEAT.name(), () -> loadRepeats());


        NowStats nowStats = new NowStats(
                windowCounters[0], windowCounters[1],windowCounters[2], round1(windowCounters[0] / 60.0),
                topKeyword,
                topKeywords,
                tlds,
                registrars,
                rising,
                prefix,
                suffix,
                format,
                repeatSldsEntries,
                hourly
        );

        Long lastCycle = timer.time("meta", () -> lastCycleMillis());

        return new StatsPayload(System.currentTimeMillis(),
                lastCycle, nowStats, new TodayStats(movers));


    }

    @Override
    public long[] counter(LocalDateTime from, LocalDateTime now) {

        Object[] windowCounters = domainRepository.getWindowCounters
                (from, now.minusMinutes(10), now.minusMinutes(1), now, PARSED).getFirst();

        return new long[] { longNullSafe(windowCounters[0]), longNullSafe(windowCounters[1]), longNullSafe(windowCounters[2])};
    }

    @Override
    public List<KeywordEntry> topKeywords(LocalDateTime from, LocalDateTime now, long total){
        List<KeywordStatsProjection> rows =
                domainKeywordRepository.getKeywordStats(from, now, PageRequest.of(0,
                        snapshotProperties.topKeywords() + OVERFETCH));

        return rows.stream()
                .filter(k -> !KeywordRules.isNumeric(k.keyword()))
                .limit(snapshotProperties.topKeywords())
                .map(k -> new KeywordEntry(k.keyword(), k.count(),
                        k.prefixCount(), k.suffixCount(), KeywordRules.isStop(k.keyword())))
                .toList();
    }

    @Override
    public List<TldEntry> tlds(LocalDateTime from, LocalDateTime now, long total){
        List<Object[]> rows = domainRepository.getTldCounts(from, now, PARSED,
                PageRequest.of(0, snapshotProperties.topTlds()));
        List<TldEntry> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            long count = longNullSafe(r[1]);
            out.add(new TldEntry((String) r[0], count, total == 0 ? 0 : round1(count * 100.0 / total)));
        }
        return out;
    }

    @Override
    public List<RegistrarEntry> registrars(LocalDateTime from, LocalDateTime now, long total) {
        List<Object[]> topRegistrars = domainRepository.getRegistrarCounts(from, now, PARSED, PageRequest.of(0, snapshotProperties.topRegistrars()));
        List<RegistrarEntry> out = new ArrayList<>(topRegistrars.size() + 1);
        long sum = 0;
        for (Object[] reg : topRegistrars) {
            Long id = longNullSafe(reg[0]);
            long count = longNullSafe(reg[1]);
            sum += count;
            out.add(new RegistrarEntry(id, registrarDictionary.getName(id), count));
        }
        if (total > sum) {
            out.add(new RegistrarEntry(null, "Others", total - sum));
        }
        return out;
    }

    private Long lastCycleMillis() {
        LocalDateTime t = domainRepository.findLatestCreatedAt();
        return t == null
                ? null
                : t.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    // heavy

    @Override
    public Map<String, List<RisingEntry>> loadRising() {
        LocalDateTime now = LocalDateTime.now();
        List<RisingKeywordProjection> rows = domainKeywordRepository.getRisingKeywordStats(
                now.minusMinutes(15), now.minusMinutes(30),
                now.minusHours(1), now.minusHours(2),
                now.minusHours(3), now.minusHours(6),
                now, snapshotProperties.risingMinRecent());

        Map<String, List<RisingEntry>> out = new LinkedHashMap<>();
        out.put("15m", rank(rows, RisingKeywordProjection::recent15m, RisingKeywordProjection::prior15m));
        out.put("1h", rank(rows, RisingKeywordProjection::recent1h, RisingKeywordProjection::prior1h));
        out.put("3h", rank(rows, RisingKeywordProjection::recent3h, RisingKeywordProjection::prior3h));
        return out;
    }

    @Override
    public List<RisingEntry> rank(List<RisingKeywordProjection> rows,
                                  ToLongFunction<RisingKeywordProjection> recentOf,
                                  ToLongFunction<RisingKeywordProjection> priorOf) {
        List<RisingEntry> list = new ArrayList<>();
        for (RisingKeywordProjection r : rows) {
            long recent = recentOf.applyAsLong(r);
            long prior = priorOf.applyAsLong(r);
            if (recent < snapshotProperties.risingMinRecent() || !KeywordRules.isRankable(r.keyword())) {
                continue;
            }
            list.add(new RisingEntry(r.keyword(), recent, prior, round1((double) recent / Math.max(prior, 1))));
        }
        list.sort(Comparator.comparingDouble(RisingEntry::lift)
                .thenComparingLong(RisingEntry::recent)
                .reversed()
                .thenComparing(RisingEntry::word));
        return list.stream().limit(snapshotProperties.risingLimit()).toList();
    }



    @Override
    public List<WordEntry> edges(boolean prefix, LocalDateTime from, LocalDateTime now) {
        PageRequest page = PageRequest.of(0, snapshotProperties.topPrefixSuffix() + OVERFETCH);
        List<Object[]> rows = prefix ? domainKeywordRepository.getPrefixCounts(from, now, page)
                : domainKeywordRepository.getSuffixCounts(from, now, page);
        return rows.stream()
                .filter(r -> KeywordRules.isRankable((String) r[0]))
                .limit(snapshotProperties.topPrefixSuffix())
                .map(r -> new WordEntry((String) r[0], longNullSafe(r[1])))
                .toList();
    }

    @Override
    public FormatEntry loadFormat(LocalDateTime from, LocalDateTime now) {
        Object[] r = domainRepository.getFormatCounts(from, now, PARSED).getFirst();
        long total = longNullSafe(r[0]);
        return new FormatEntry(total, longNullSafe(r[4]), longNullSafe(r[5]), longNullSafe(r[3]), longNullSafe(r[1]), longNullSafe(r[2]));
    }

    @Override
    public List<HourEntry> loadHourly() {
        LocalDateTime end = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        LocalDateTime start = end.minusHours(23);

        Map<LocalDateTime, Long> counts = new HashMap<>();
        for (Object[] r : domainRepository.getHourlyCounts(start, end.plusHours(1), PARSED)) {
            counts.put(LocalDateTime.of((int) longNullSafe(r[0]), (int) longNullSafe(r[1]), (int) longNullSafe(r[2]), (int) longNullSafe(r[3]), 0), longNullSafe(r[4]));
        }
        List<HourEntry> out = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {                       // always 24 entries, empty hours are 0
            LocalDateTime h = start.plusHours(i);
            out.add(new HourEntry(h.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    counts.getOrDefault(h, 0L)));
        }
        return out;
    }

    @Override
    public List<MoverEntry> loadMovers() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();

        Map<String, Long> today = new LinkedHashMap<>();
        for (Object[] r : domainKeywordRepository.getTodayKeywordCounts(todayStart, now, snapshotProperties.moversMinToday(),
                PageRequest.of(0, snapshotProperties.moversCandidates()))) {
            String word = (String) r[0];
            if (KeywordRules.isRankable(word)) {
                today.put(word, longNullSafe(r[1]));
            }
        }
        if (today.isEmpty()) {
            return List.of();
        }

        Map<String, Long> history = new HashMap<>();
        for (Object[] r : domainKeywordRepository.getBaselineCounts(today.keySet(), todayStart.minusDays(7), todayStart)) {
            history.put((String) r[0], longNullSafe(r[1]));
        }

        LocalDateTime earliest = domainRepository.findEarliestRegisteredAt(PARSED);
        long days = earliest == null ? 1 : Math.max(1, Math.min(7, ChronoUnit.DAYS.between(earliest, todayStart)));

        List<MoverEntry> out = new ArrayList<>(today.size());
        today.forEach((word, count) -> {
            double baseline = history.getOrDefault(word, 0L) / (double) days;
            out.add(new MoverEntry(word, count, round1(baseline), round1(count / Math.max(baseline, 1.0))));
        });
        out.sort(Comparator.comparingDouble(MoverEntry::lift).reversed().thenComparing(MoverEntry::word));
        return out.stream().limit(snapshotProperties.moversLimit()).toList();
    }

    @Override
    public List<RepeatEntry> loadRepeats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusHours(1);

        List<Object[]> top = domainRepository.getRepeatedSlds(from, now, PARSED, MIN_REPEAT_TLDS,
                PageRequest.of(0, REPEATS_LIMIT));
        if (top.isEmpty()) {
            return List.of();
        }
        List<String> slds = top.stream().map(r -> (String) r[0]).toList();

        Map<String, List<String>> tldsBySld = new HashMap<>();
        Map<String, Long> firstDomainId = new HashMap<>();
        for (Object[] r : domainRepository.getSldTldsFor(slds, from, now, PARSED)) {
            String sld = (String) r[0];
            tldsBySld.computeIfAbsent(sld, k -> new ArrayList<>()).add((String) r[1]);
            firstDomainId.putIfAbsent(sld, longNullSafe(r[2]));
        }

        List<RepeatEntry> out = new ArrayList<>(top.size());
        for (Object[] r : top) {
            String sld = (String) r[0];
            List<String> tldList = tldsBySld.getOrDefault(sld, List.of()).stream().limit(REPEATS_TLD_LIST).toList();
            Long id = firstDomainId.get(sld);
            out.add(new RepeatEntry(sld,
                    (int) longNullSafe(r[1]),
                    tldList));
        }
        return out;
    }



    // helper

    private static long longNullSafe(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

}


