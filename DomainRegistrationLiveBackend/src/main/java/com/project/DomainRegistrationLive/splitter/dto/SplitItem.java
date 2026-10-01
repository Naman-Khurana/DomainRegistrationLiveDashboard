package com.project.DomainRegistrationLive.splitter.dto;

import java.util.UUID;

public record SplitItem(
        UUID domainId,
        String domainName,
        String sld
) {
}