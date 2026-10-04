package com.project.DomainRegistrationLive.service.impl;

import com.project.DomainRegistrationLive.dto.request.IngestRequest;
import com.project.DomainRegistrationLive.dto.response.IngestResponse;
import com.project.DomainRegistrationLive.entity.Domain;
import com.project.DomainRegistrationLive.enums.DomainStatus;
import com.project.DomainRegistrationLive.exception.DuplicateResourceException;
import com.project.DomainRegistrationLive.mapper.DomainMapper;
import com.project.DomainRegistrationLive.repository.DomainRepository;
import com.project.DomainRegistrationLive.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.project.DomainRegistrationLive.exception.ErrorCodes.DUPLICATE_DOMAIN;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionServiceImpl implements IngestionService {

    private final DomainRepository domainRepository;
    private final DomainMapper domainMapper;

    @Override
    public Domain buildDomain(String extractedDomain, int registrarId, LocalDateTime registeredAt) {

        String[] domainArr = extractedDomain.split("\\.");
        String sld = domainArr[0];
        String tld = String.join(".", Arrays.copyOfRange(domainArr, 1, domainArr.length));


        Domain domain = Domain.builder()
                .name(extractedDomain)
                .sld(sld)
                .tld(tld)
                .registrarId(registrarId)
                .registeredAt(registeredAt)
                .attempts(0)
                .status(DomainStatus.PENDING)
                .build();

        return domain;

    }


    @Override
    @Transactional
    public List<IngestResponse> ingestAll(List<IngestRequest> requests){

        List<String> names = requests.stream()
                .map(request -> extractHost(request.name()))
                .distinct()
                .toList();

        List<Domain> existingDomains = domainRepository.findByNameIn(names);

        Set<String> seenNames = existingDomains.stream()
                .map(domain -> domain.getName())
                .collect(Collectors.toSet());



        List<Domain> domains = new ArrayList<>();

        for(IngestRequest request : requests){
            String extractedDomain = extractHost(request.name());

            String[] domainArr = extractedDomain.split("\\.");
            String tld = String.join(
                    ".",
                    Arrays.copyOfRange(domainArr, 1, domainArr.length)
            );

            if (EXCLUDED_TLDS.contains("." + tld)) {
                log.info("Skipping domain with excluded TLD: {}", extractedDomain);
                continue;
            }

            if(!seenNames.add(extractedDomain)){
                log.info("Skipping duplicate registration for domain: {}", extractedDomain);
                continue;
            }



            Domain newDomain = buildDomain(extractedDomain, request.registrarId(), request.registeredAt());
            domains.add(newDomain);
        }
        domainRepository.saveAll(domains);

        return domainMapper.toIngestResponseList(domains);

    }


    @Override
    public String extractHost(String raw) {
        String s = raw.trim().toLowerCase();

        // 1. remove the scheme (https://) if present
        int scheme = s.indexOf("://");
        if (scheme >= 0) s = s.substring(scheme + 3);

        // 2. cut off the path, query and fragment
        int cut = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '/' || c == '?' || c == '#') { cut = i; break; }
        }
        if (cut >= 0) s = s.substring(0, cut);

        // 3. remove login info (user:pass@) if present
        int at = s.lastIndexOf('@');
        if (at >= 0) s = s.substring(at + 1);

        // 4. remove the port (:8080) if present
        int colon = s.indexOf(':');
        if (colon >= 0) s = s.substring(0, colon);

        // 5. remove trailing dots
        while (s.endsWith(".")) s = s.substring(0, s.length() - 1);

        // 6. remove a leading "www." (only if a real domain is left after it)
        if (s.startsWith("www.") && s.indexOf('.', 4) > 0) {
            s = s.substring(4);
        }
        return s;
    }
}
