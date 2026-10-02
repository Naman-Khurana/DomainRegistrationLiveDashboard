package com.project.DomainRegistrationLive.dto;

import com.project.DomainRegistrationLive.dto.response.RisingKeyword;

import java.util.List;

public record RisingKeywordAnalytics(
        List<RisingKeyword> m15,
        List<RisingKeyword> h1,
        List<RisingKeyword> h3
) {}
