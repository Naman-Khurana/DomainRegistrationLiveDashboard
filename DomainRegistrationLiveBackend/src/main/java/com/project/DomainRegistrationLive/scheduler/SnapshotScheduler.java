//package com.project.DomainRegistrationLive.scheduler;
//
//import com.project.DomainRegistrationLive.service.SnapshotService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class SnapshotScheduler {
//
//    private final SnapshotService snapshotService;
//
//    @Scheduled(fixedDelayString = "${snapshot.interval}")
//    public void createSnapshot() {
//        snapshotService.createSnapshot();
//    }
//}
