package com.project.DomainRegistrationLive.splitter.dto;

import java.util.List;
import java.util.UUID;

public record SplitResult(
        UUID domainId,
        List<String> keywords,
        String sld
) {
}