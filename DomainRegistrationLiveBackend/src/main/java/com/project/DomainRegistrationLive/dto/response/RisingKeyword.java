package com.project.DomainRegistrationLive.dto.response;

public record RisingKeyword(
        String word,
        Long recent,
        Long prior,
        Double lift
) {}
