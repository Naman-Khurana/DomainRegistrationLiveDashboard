package com.project.DomainRegistrationLive.dto;

public record KeywordStat(
        String word,
        Long count,
        Long pfx,
        Long sfx
) {}