package com.project.DomainRegistrationLive.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record IngestRequest(

        @NotBlank(message = "Domain name is required")
        @Size(max = 255, message = "Domain name must not exceed 255 characters")
        String name,

        @Positive(message = "Registrar ID must be positive")
        Integer registrarId,

        @NotNull(message = "Registered time is required")
        LocalDateTime registeredAt
) {
}