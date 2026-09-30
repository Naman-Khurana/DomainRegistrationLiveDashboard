package com.project.DomainRegistrationLive.service;

import com.project.DomainRegistrationLive.dto.request.IngestRequest;
import com.project.DomainRegistrationLive.dto.response.IngestResponse;

public interface IngestionService {

    IngestResponse ingest(IngestRequest request);

    String extractHost(String raw);
}
