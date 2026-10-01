package com.project.DomainRegistrationLive.service.impl;

import com.project.DomainRegistrationLive.entity.Domain;
import com.project.DomainRegistrationLive.enums.DomainStatus;
import com.project.DomainRegistrationLive.repository.DomainKeywordRepository;
import com.project.DomainRegistrationLive.repository.DomainRepository;
import com.project.DomainRegistrationLive.service.SegmentationService;
import com.project.DomainRegistrationLive.service.SegmentationWriterService;
import com.project.DomainRegistrationLive.splitter.SplitterClient;
import com.project.DomainRegistrationLive.splitter.dto.SplitItem;
import com.project.DomainRegistrationLive.splitter.dto.SplitResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SegmentationServiceImpl implements SegmentationService {

    private final DomainRepository domainRepository;
    private final SplitterClient splitterClient;
    private final DomainKeywordRepository domainKeywordRepository;
    private final SegmentationWriterService segmentationWriterService;

    @Override
    public int processBatch() {
        List<Domain> domains = domainRepository.findTop100ByStatusOrderByCreatedAtAscIdAsc(DomainStatus.PENDING);

        if(domains.isEmpty()){
            return 0;
        }

        List<SplitItem> items = domains.stream()
                .map(domain -> new SplitItem(
                        domain.getId(),
                        domain.getName(),
                        domain.getSld()
                ))
                .toList();

        SplitResponse response = splitterClient.split(items);

        segmentationWriterService.buildDomainKeywordsAndSave(domains,response);

        return domains.size();
    }


}
