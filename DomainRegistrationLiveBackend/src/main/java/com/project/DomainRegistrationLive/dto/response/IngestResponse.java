package com.project.DomainRegistrationLive.dto.response;

import com.project.DomainRegistrationLive.enums.DomainStatus;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

public record IngestResponse (
        String name,
        String sld,
        String tld,
        LocalDateTime registeredAt,
        DomainStatus status
){

}
