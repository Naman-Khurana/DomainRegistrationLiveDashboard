package com.project.DomainRegistrationLive.service;

import com.project.DomainRegistrationLive.dto.request.IngestRequest;
import com.project.DomainRegistrationLive.dto.response.IngestResponse;
import com.project.DomainRegistrationLive.entity.Domain;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface IngestionService {
     static final Set<String> EXCLUDED_TLDS = new HashSet<>(
            Arrays.asList(
                    ".ph", ".de", ".ir", ".es", ".eu", ".at", ".vn", ".gr", ".ae",
                    ".li", ".im", ".lv", ".ge", ".np", ".pe", ".st", ".sa", ".bz",
                    ".lk", ".bd", ".hr", ".zw", ".md", ".bg", ".by", ".az", ".uy",
                    ".lu", ".tn", ".py", ".tm", ".ao", ".cy", ".so", ".vc", ".ci",
                    ".ee", ".ba", ".mn", ".mk", ".gt", ".qa", ".la", ".et", ".mt",
                    ".ve", ".mz", ".tj", ".ag", ".om", ".ug", ".bo", ".iq", ".mv",
                    ".sy", ".edu", ".gl", ".do", ".cd", ".nc", ".lc"
            )
    );

    Domain buildDomain(String extractedDomain, int registrarId, LocalDateTime registeredAt);

    List<IngestResponse> ingestAll(List<IngestRequest> requests);

    String extractHost(String raw);
}
