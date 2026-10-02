package com.project.DomainRegistrationLive.dto.projection;

public record RisingKeywordProjection(
        String keyword,
        Long recent15m,
        Long prior15m,
        Long recent1h,
        Long prior1h,
        Long recent3h,
        Long prior3h
) {}