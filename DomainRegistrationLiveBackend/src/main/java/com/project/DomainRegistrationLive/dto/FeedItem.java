package com.project.DomainRegistrationLive.dto;

import java.util.List;

public record FeedItem(
        long seq,
        String domain,
        String tld,
        List<String> keywords,
        long t
) {
}
