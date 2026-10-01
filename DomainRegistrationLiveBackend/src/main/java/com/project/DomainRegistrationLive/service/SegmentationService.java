package com.project.DomainRegistrationLive.service;

import com.project.DomainRegistrationLive.entity.Domain;
import com.project.DomainRegistrationLive.splitter.dto.SplitResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SegmentationService {
    public int processBatch();

}
