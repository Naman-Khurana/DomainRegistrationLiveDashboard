//package com.project.DomainRegistrationLive.service.impl;
//
//import com.project.DomainRegistrationLive.entity.Domain;
//import com.project.DomainRegistrationLive.entity.Snapshot;
//import com.project.DomainRegistrationLive.enums.DomainStatus;
//import com.project.DomainRegistrationLive.repository.DomainRepository;
//import com.project.DomainRegistrationLive.repository.SnapshotRepository;
//import com.project.DomainRegistrationLive.service.SnapshotService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class SnapshotServiceImpl implements SnapshotService {
//
//    private final SnapshotRepository snapshotRepository;
//    private final DomainRepository domainRepository;
//
//    @Override
//    public void createSnapshot() {
//        Snapshot prevSnapshot = snapshotRepository.findTopByOrderByIdDesc().orElse(null);
//
//        long lastDomainPassedInFeed = prevSnapshot != null ? prevSnapshot.getToSeq() : -1 ;
//
//
//        List<Domain> newFeed = domainRepository
//                .findByIdGreaterThanAndStatusOrderByIdAsc(lastDomainPassedInFeed, DomainStatus.PARSED);
//
//
//
//
//
//    }
//}
