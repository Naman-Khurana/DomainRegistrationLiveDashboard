package com.project.DomainRegistrationLive.service;

import com.project.DomainRegistrationLive.dto.request.IngestRequest;
import com.project.DomainRegistrationLive.dto.response.IngestResponse;
import com.project.DomainRegistrationLive.entity.Domain;

import java.time.LocalDateTime;
import java.util.List;

public interface IngestionService {

    Domain buildDomain(String extractedDomain, int registrarId, LocalDateTime registeredAt);

    List<IngestResponse> ingestAll(List<IngestRequest> requests);

    String extractHost(String raw);
}
