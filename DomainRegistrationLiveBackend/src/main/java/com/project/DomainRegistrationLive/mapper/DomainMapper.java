package com.project.DomainRegistrationLive.mapper;

import com.project.DomainRegistrationLive.dto.response.IngestResponse;
import com.project.DomainRegistrationLive.entity.Domain;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DomainMapper  {

    IngestResponse toIngestResponse(Domain domain);

    List<IngestResponse> toIngestResponseList(List<Domain> domains);
}

