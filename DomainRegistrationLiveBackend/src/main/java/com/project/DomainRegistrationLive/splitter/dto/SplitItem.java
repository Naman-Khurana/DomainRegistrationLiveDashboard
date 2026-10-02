package com.project.DomainRegistrationLive.splitter.dto;

import java.util.UUID;

public record SplitItem(
        Long domainId,
        String domainName,
        String sld
) {
}