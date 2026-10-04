package com.project.DomainRegistrationLive.dto.response;

import com.project.DomainRegistrationLive.dto.SnapshotModels;

import java.time.LocalDate;
import java.util.List;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

public record DailySummaryResponse(
        LocalDate date,
        List<RegistrarEntry> entries

) {
}
