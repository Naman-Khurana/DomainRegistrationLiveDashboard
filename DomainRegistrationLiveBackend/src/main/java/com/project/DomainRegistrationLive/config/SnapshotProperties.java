package com.project.DomainRegistrationLive.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

@ConfigurationProperties(prefix = "segmentation")
public record SnapshotProperties (
        @DefaultValue("500") int snapshotsToKeep,
        @DefaultValue("10s") String interval,
        @DefaultValue("3s") String initialDelay,
        @DefaultValue("4000") long slowBuildWarnMs,
        @DefaultValue("40") int topKeywords,
        @DefaultValue("40") int topTlds,
        @DefaultValue("15") int topRegistrars,
        @DefaultValue("5") int risingMinRecent,
        @DefaultValue("15") int risingLimit,
        @DefaultValue("10") int topPrefixSuffix,
        @DefaultValue("18") int moversLimit,
        @DefaultValue("30") int moversMinToday,
        @DefaultValue("500") int moversCandidates,


        Map<Long, String> registrarNames
) {

    public SnapshotProperties {
        if (registrarNames == null) {
            registrarNames = Map.of();
        }
    }

    public String registrarName(Long id) {
        return id == null ? null : registrarNames.getOrDefault(id, "IANA #" + id);
    }
}
