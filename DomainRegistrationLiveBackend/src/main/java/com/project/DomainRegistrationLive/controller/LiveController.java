package com.project.DomainRegistrationLive.controller;

import com.project.DomainRegistrationLive.dto.response.SearchResponse;
import com.project.DomainRegistrationLive.service.LiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

@RestController
@RequestMapping("/v1/live")
@RequiredArgsConstructor
public class LiveController {

    private final LiveService liveService;



    @GetMapping("/snapshot")
    public ResponseEntity<SnapshotResponse> snapshot() throws InterruptedException {
        return ResponseEntity.ok()
                .body(liveService.latestSnapshot().orElse(null));
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedResponse> feed() {
        return ResponseEntity.ok().body(liveService.initialFeed().orElse(null));

    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam String q,
                                                 @RequestParam(required = false, defaultValue = "0")
                                                  String h) {
        return ResponseEntity.ok().body(liveService.search(q, Integer.parseInt(h)));
    }




}

