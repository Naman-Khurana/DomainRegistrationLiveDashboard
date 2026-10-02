package com.project.DomainRegistrationLive.timer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SectionTimer {

    private final Map<String, Long> millis = new LinkedHashMap<>();
    private final long startNanos = System.nanoTime();

    public <T> T time(String section, Supplier<T> work) {
        long t0 = System.nanoTime();
        try {
            return work.get();
        } finally {
            millis.put(section, (System.nanoTime() - t0) / 1_000_000);
        }
    }

    public void run(String section, Runnable work) {
        time(section, () -> {
            work.run();
            return null;
        });
    }

    public long totalMillis() {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    public String summary() {
        StringBuilder sb = new StringBuilder("total=").append(totalMillis()).append("ms [");
        millis.forEach((k, v) -> sb.append(k).append('=').append(v).append(' '));
        return sb.toString().trim() + "]";
    }
}
