package com.project.DomainRegistrationLive.service.impl;

import com.project.DomainRegistrationLive.entity.Domain;
import com.project.DomainRegistrationLive.entity.DomainKeyword;
import com.project.DomainRegistrationLive.enums.DomainStatus;
import com.project.DomainRegistrationLive.repository.DomainKeywordRepository;
import com.project.DomainRegistrationLive.repository.DomainRepository;
import com.project.DomainRegistrationLive.service.SegmentationWriterService;
import com.project.DomainRegistrationLive.splitter.SplitterException;
import com.project.DomainRegistrationLive.splitter.dto.SplitResponse;
import com.project.DomainRegistrationLive.splitter.dto.SplitResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.project.DomainRegistrationLive.splitter.SplitConstants.SPLITTER_INVALID_RESPONSE;

@Component
@RequiredArgsConstructor
@Slf4j
public class SegmentationWriterServiceImpl implements SegmentationWriterService {

    private final DomainKeywordRepository domainKeywordRepository;
    private final DomainRepository domainRepository;

    @Transactional
    @Override
    public void buildDomainKeywordsAndSave(List<Domain> domains, SplitResponse response){
        Map<UUID, List<String>> keywordsByDomainId =
                response.results().stream()
                        .collect(Collectors.toMap(
                                SplitResult::domainId,
                                SplitResult::keywords
                        ));

        List<DomainKeyword> domainKeywords = new ArrayList<>();

        for(Domain domain : domains){
            List<String> keywords =keywordsByDomainId.get(domain.getId());

            if(keywords == null || keywords.isEmpty()) {
                throw new SplitterException(SPLITTER_INVALID_RESPONSE,"Invalid splitter result for domain: " + domain.getId());
            }

            short position = 1;
            for(String keyword : keywords){
                DomainKeyword domainKeyword = DomainKeyword.builder()
                        .domain(domain)
                        .position(position)
                        .keyword(keyword)
                        .registeredAt(domain.getRegisteredAt())
                        .isFirst(position == 1)
                        .isLast(position == (keywords.size()))
                        .build();

                domainKeywords.add(domainKeyword);
                position++;
            }

            domain.setStatus(DomainStatus.PARSED);
        }

        domainKeywordRepository.saveAll(domainKeywords);
        domainRepository.saveAll(domains);

    }
}
