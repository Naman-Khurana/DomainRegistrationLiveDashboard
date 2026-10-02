package com.project.DomainRegistrationLive.splitter.dto;

import java.util.List;


public record SplitResult(
        Long domainId,
        List<String> keywords,
        String sld
) {
}