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

import java.util.Arrays;

import static com.project.DomainRegistrationLive.exception.ErrorCodes.DUPLICATE_DOMAIN;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionServiceImpl implements IngestionService {

    private final DomainRepository domainRepository;
    private final DomainMapper domainMapper;

    @Override
    @Transactional
    public IngestResponse ingest(IngestRequest request) {
        if(domainRepository.findByName(request.name())){
            throw new DuplicateResourceException(DUPLICATE_DOMAIN, "Domain with this name already exists");
        }
        String extractedDomain = extractHost(request.name());
        String[] domainArr = extractedDomain.split("\\.");
        String sld = domainArr[0];
        String tld = String.join(".", Arrays.copyOfRange(domainArr, 1, domainArr.length));

        Domain domain = Domain.builder()
                .name(extractedDomain)
                .sld(sld)
                .tld(tld)
                .registrarId(request.registrarId())
                .registeredAt(request.registeredAt())
                .status(DomainStatus.PENDING)
                .build();

        domainRepository.save(domain);

        return domainMapper.toIngestResponse(domain);

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
