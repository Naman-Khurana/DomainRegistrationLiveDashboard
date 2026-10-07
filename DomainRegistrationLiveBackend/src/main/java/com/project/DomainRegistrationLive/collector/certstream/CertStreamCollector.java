package com.project.DomainRegistrationLive.collector.certstream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.project.DomainRegistrationLive.collector.CollectorProperties;
import com.project.DomainRegistrationLive.collector.HostThrottle;
import com.project.DomainRegistrationLive.collector.dto.RdapResult;
import com.project.DomainRegistrationLive.collector.dto.RdapService;
import com.project.DomainRegistrationLive.collector.rdap.RdapClient;
import com.project.DomainRegistrationLive.dto.request.IngestRequest;
import com.project.DomainRegistrationLive.dto.response.IngestResponse;
import com.project.DomainRegistrationLive.service.IngestionService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Component
@Slf4j
@ConditionalOnProperty(prefix = "collector", name = "enabled", havingValue = "true")
public class CertStreamCollector {

    private final CollectorProperties collectorProperties;
    private final RdapClient rdapClient;
    private final CertStreamClient certStreamClient;
    private final IngestionService ingestionService;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private Cache<String, Boolean> seen;          // domains already looked up (bounded, expires)
    private BlockingQueue<String> candidates;     // domains waiting for an RDAP lookup
    private BlockingQueue<IngestRequest> results; // confirmed domains waiting to be saved
    private HostThrottle hostThrottle;

    private ExecutorService rdapPool;
    private ExecutorService flusherPool;
    private ScheduledExecutorService housekeeping;

    private static final Duration BOOTSTRAP_MAX_AGE = Duration.ofHours(24);

    @PostConstruct
    public void init() {
        seen = CacheBuilder.newBuilder()
                .maximumSize(collectorProperties.seenMaxSize())
                .expireAfterWrite(collectorProperties.seenTtl())
                .build();
        candidates = new ArrayBlockingQueue<>(collectorProperties.candidateQueueSize());
        results = new ArrayBlockingQueue<>(collectorProperties.resultQueueSize());
        hostThrottle = new HostThrottle(collectorProperties.rdapMaxPerHostPerSecond());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        rdapClient.refreshIfStale(BOOTSTRAP_MAX_AGE);

        rdapPool = Executors.newFixedThreadPool(collectorProperties.rdapWorkers(), daemonFactory("rdap"));
        for (int i = 0; i < collectorProperties.rdapWorkers(); i++) {
            rdapPool.execute(this::workerLoop);
        }

        flusherPool = Executors.newFixedThreadPool(1, daemonFactory("flusher"));
        flusherPool.execute(this::flusherLoop);

        housekeeping = Executors.newSingleThreadScheduledExecutor(daemonFactory("housekeeping"));
        housekeeping.scheduleWithFixedDelay(
                safely(() -> rdapClient.refreshIfStale(BOOTSTRAP_MAX_AGE)), 5, 5, TimeUnit.MINUTES);

        // Start the stream last: everything its callback feeds is already running.
        certStreamClient.start(this::onMessage);
        log.info("CertStream collector started (stream: {}, max age: {})",
                collectorProperties.certstreamUrl(), collectorProperties.maxAge());
    }

    @PreDestroy
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        certStreamClient.stop();
        if (rdapPool != null) {
            rdapPool.shutdownNow();
        }
        if (flusherPool != null) {
            flusherPool.shutdownNow();
        }
        if (housekeeping != null) {
            housekeeping.shutdownNow();
        }
        log.info("CertStream collector stopped.");
    }

    private ThreadFactory daemonFactory(String name) {
        AtomicInteger counter = new AtomicInteger(1);
        return task -> {
            Thread thread = new Thread(task, "collector-" + name + "-" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }

    // stage 1: stream thread
    private void onMessage(String json) {
        try {
            if (!running.get()) {
                return;
            }

            for (String domainName : CertStreamExtractor.registeredDomains(json)) {

                if (isExcludedTld(domainName)) {
                    continue;
                }
                // atomic: only the first sighting of a name gets through
                if (seen.asMap().putIfAbsent(domainName, Boolean.TRUE) != null) {
                    continue;
                }
                // non-blocking; if lookups cannot keep up the candidate is dropped and may be seen again later
                if (!candidates.offer(domainName)) {
                    seen.invalidate(domainName);
                }
            }
        } catch (RuntimeException e) {
            log.error("collector could not handle a cert stream message", e);
        }
    }

    private static boolean isExcludedTld(String registeredDomain) {
        String tld = registeredDomain.substring(registeredDomain.indexOf('.') + 1);
        return IngestionService.EXCLUDED_TLDS.contains("." + tld);
    }

    // stage 2: RDAP workers

    private void workerLoop() {
        while (running.get()) {
            try {
                String name = candidates.poll(1, TimeUnit.SECONDS);   // timed, so the loop notices stop()
                if (name != null) {
                    process(name);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.warn("collector: RDAP worker error", e);
            }
        }
    }

    private void process(String domainName) throws InterruptedException {
        Optional<RdapService> rdapService = rdapClient.serviceFor(domainName);
        if (rdapService.isEmpty()) {
            if (!rdapClient.isReady()) {
                seen.invalidate(domainName);   // the bootstrap has not loaded yet: try again later
            }
            return;
        }

        String host = rdapService.get().host();
        if (!hostThrottle.acquire(host)) {
            seen.invalidate(domainName);
            return;
        }

        RdapResult rdapResult = rdapClient.lookup(rdapService.get(), domainName);

        if (rdapResult instanceof RdapResult.Confirmed confirmed) {
            onConfirmed(domainName, confirmed);
        } else if (rdapResult instanceof RdapResult.NotFound) {
        } else if (rdapResult instanceof RdapResult.RateLimited limited) {
            hostThrottle.pause(host, limited.retryAfter());
            seen.invalidate(domainName);
        } else {
            seen.invalidate(domainName);
        }
    }

    private void onConfirmed(String name, RdapResult.Confirmed confirmed) throws InterruptedException {
        // Use system default zone (Asia/Kolkata), aligned with RdapClient and Hibernate
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime registeredAt = confirmed.registeredAt();

        if (registeredAt.isBefore(now.minus(collectorProperties.maxAge()))) {
            return;
        }
        if (registeredAt.isAfter(now.plusMinutes(5))) {
            return;
        }

        IngestRequest request = new IngestRequest(name, confirmed.registrarId(), registeredAt);
        if (!results.offer(request, 5, TimeUnit.SECONDS)) {   // timed backpressure from a slow database
            seen.invalidate(name);
        }
    }

    // stage 3: the single writer
    private void flusherLoop() {
        List<IngestRequest> batch = new ArrayList<>(collectorProperties.batchSize());
        while (running.get()) {
            try {
                IngestRequest first = results.poll(collectorProperties.flushInterval().toMillis(), TimeUnit.MILLISECONDS);
                if (first != null) {
                    batch.add(first);
                    results.drainTo(batch, collectorProperties.batchSize() - 1);
                    flush(batch);
                    batch.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (RuntimeException e) {
                log.warn("collector: flusher error", e);
                batch.clear();
            }
        }
    }

    private void flush(List<IngestRequest> batch) {
        try {
            List<IngestResponse> saved = ingestionService.ingestAll(List.copyOf(batch));
            log.debug("collector: saved {} of {} domains", saved.size(), batch.size());
        } catch (RuntimeException e) {
            log.error("collector: ingesting {} domains failed", batch.size(), e);
            batch.forEach(r -> seen.invalidate(r.name()));
        }
    }

    // housekeeping


    private static Runnable safely(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (RuntimeException e) {
                log.warn("collector: housekeeping task failed", e);
            }
        };
    }
}