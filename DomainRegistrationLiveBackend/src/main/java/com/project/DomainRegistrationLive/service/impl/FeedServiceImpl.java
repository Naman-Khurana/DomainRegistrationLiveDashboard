package com.project.DomainRegistrationLive.service.impl;

import com.project.DomainRegistrationLive.config.SnapshotProperties;
import com.project.DomainRegistrationLive.entity.Domain;
import com.project.DomainRegistrationLive.enums.DomainStatus;
import com.project.DomainRegistrationLive.repository.DomainRepository;
import com.project.DomainRegistrationLive.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedServiceImpl implements FeedService {

    private final DomainRepository domainRepository;
    private final SnapshotProperties snapshotProperties;

    @Override
    public List<FeedEntry> read(long afterId, long toId) {
        if(toId <= afterId)
            return List.of();

        List<Domain> newestFirst = domainRepository.findByStatusAndIdGreaterThanAndIdLessThanEqualOrderByIdDesc(
                DomainStatus.PARSED,afterId,toId, Pageable.unpaged()
        );

//        Collections.reverse(newestFirst);
        return toEntries(newestFirst);
    }

    //for new session
    @Override
    public List<FeedEntry> latest(long toId, int limit) {
        List<Domain> newestFirst = domainRepository.findByStatusAndIdLessThanEqualOrderByIdDesc(
                DomainStatus.PARSED, toId, PageRequest.of(0, limit));

        return toEntries(newestFirst);
    }

    private List<FeedEntry> toEntries(List<Domain> newestFirst){
        if (newestFirst.isEmpty()) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>(newestFirst.size());
        for(Domain d : newestFirst){
            ids.add(d.getId());
        }

        List<FeedEntry> out = new ArrayList<>(newestFirst.size());
        for (Domain d : newestFirst) {
            Long registrarId = d.getRegistrarId() == null ? null : ((Number) d.getRegistrarId()).longValue();
            out.add(new FeedEntry(
                    d.getId(),
                    d.getName(),
                    d.getTld(),

                    registrarId,
                    snapshotProperties.registrarName(registrarId),
                    d.getRegisteredAt()
                            .atZone(ZoneId.of("Asia/Kolkata"))
                            .toInstant()
                            .toEpochMilli()
            ));
        }

        return out;
    }
}
