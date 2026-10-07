package com.project.DomainRegistrationLive.collector.certstream;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.project.DomainRegistrationLive.collector.CollectorProperties;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "collector", name = "enabled", havingValue = "true")
public class CertStreamClient {

    private static final long MAX_BACKOFF_SECONDS = 60;

    private final CollectorProperties collectorProperties;
    private HttpClient http;
    private ScheduledExecutorService scheduler;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private final AtomicBoolean reconnectPending = new AtomicBoolean(false);

    private volatile WebSocket socket;
    private volatile long lastMessageMillis;
    private volatile int attempt;
    private volatile Consumer<String> consumer = message -> { };


    @PostConstruct
    private void init(){
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "collector-certstream-reconnect");
            thread.setDaemon(true);
            return thread;
        });
    }


    public void start(Consumer<String> onMessage) {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        this.consumer = onMessage;
        lastMessageMillis = System.currentTimeMillis();

        long watchdogSeconds = Math.max(5, collectorProperties.silenceTimeout().toSeconds() / 4);
        scheduler.scheduleWithFixedDelay(this::watchdog, watchdogSeconds, watchdogSeconds, TimeUnit.SECONDS);
        connect();
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        scheduler.shutdownNow();
        WebSocket ws = socket;
        if (ws != null) {
            ws.abort();
        }
    }

    // connection handling
    private void connect() {
        if (!running.get() || !connecting.compareAndSet(false, true)) {
            return;
        }
        reconnectPending.set(false);
        log.info("CertStream: connecting to {}", collectorProperties.certstreamUrl());

        http.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .buildAsync(URI.create(collectorProperties.certstreamUrl()), new Listener())
                .whenComplete((ws, error) -> {
                    connecting.set(false);
                    if (error != null) {
                        log.warn("CertStream: connection failed: {}", rootMessage(error));
                        scheduleReconnect();
                        return;
                    }
                    socket = ws;
                    attempt = 0;
                    lastMessageMillis = System.currentTimeMillis();
                    if (!running.get()) {
                        ws.abort();
                    }
                });
    }

    private void scheduleReconnect() {
        if (!running.get() || !reconnectPending.compareAndSet(false, true)) {
            // already scheduled
            return;
        }
        long delay = Math.min(MAX_BACKOFF_SECONDS, 1L << Math.min(attempt, 6));
        attempt++;
        log.info("CertStream: reconnecting in {} s", delay);
        try {
            scheduler.schedule(this::connect, delay, TimeUnit.SECONDS);
        } catch (RejectedExecutionException e) {
            // shutting down
        }
    }

    private void watchdog() {
        try {
            if (!running.get() || connecting.get() || reconnectPending.get()) {
                return;
            }
            long silentMillis = System.currentTimeMillis() - lastMessageMillis;
            if (silentMillis > collectorProperties.silenceTimeout().toMillis()) {
                log.warn("CertStream: no message for {} s, reconnecting", silentMillis / 1000);
                WebSocket ws = socket;
                if (ws != null) {
                    ws.abort();
                }
                scheduleReconnect();
            }
        } catch (RuntimeException e) {
            log.warn("CertStream: watchdog error", e);
        }
    }

    private static String rootMessage(Throwable error) {
        Throwable t = error;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.toString();
    }

    // incoming frames

    private final class Listener implements WebSocket.Listener {
        // a message can arrive in several frames
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket ws) {
            log.info("CertStream: connected");
            ws.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                String message = buffer.toString();
                buffer.setLength(0);
                lastMessageMillis = System.currentTimeMillis();
                try {
                    consumer.accept(message);
                } catch (RuntimeException e) {
                    log.debug("CertStream: message handler failed", e);
                }
            }
            ws.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
            log.warn("CertStream: closed by the server ({} {})", statusCode, reason);
            // ignore a socket we already replaced
            if (socket == null || ws == socket) {
                scheduleReconnect();
            }
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            log.warn("CertStream: connection error: {}", rootMessage(error));
            if (socket == null || ws == socket) {
                scheduleReconnect();
            }
        }
    }
}