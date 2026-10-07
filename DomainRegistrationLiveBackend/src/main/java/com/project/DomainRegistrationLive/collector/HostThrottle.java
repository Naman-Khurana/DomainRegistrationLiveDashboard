package com.project.DomainRegistrationLive.collector;


import com.google.common.util.concurrent.RateLimiter;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class HostThrottle {
    private final double permitsPerSecond;
    private final ConcurrentMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> pausedUntilNanos = new ConcurrentHashMap<>();

    public HostThrottle(double permitsPerSecond) {
        this.permitsPerSecond = permitsPerSecond;
    }

    public boolean acquire(String host) {
        Long until = pausedUntilNanos.get(host);
        if (until != null) {
            if (System.nanoTime() - until < 0) {
                return false;
            }
            pausedUntilNanos.remove(host, until);
        }
        limiters.computeIfAbsent(host, h -> RateLimiter.create(permitsPerSecond)).acquire();
        return true;
    }

    public void pause(String host, Duration duration){
        pausedUntilNanos.put(host, System.nanoTime() + duration.toNanos());
    }

}
