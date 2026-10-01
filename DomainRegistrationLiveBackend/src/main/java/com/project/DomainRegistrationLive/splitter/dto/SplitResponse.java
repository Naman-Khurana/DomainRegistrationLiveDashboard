package com.project.DomainRegistrationLive.splitter.dto;

import java.util.List;

public record SplitResponse(
        String modelVersion,
        List<SplitResult> results
) {
}