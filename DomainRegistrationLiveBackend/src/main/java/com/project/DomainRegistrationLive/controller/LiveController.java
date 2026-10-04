package com.project.DomainRegistrationLive.controller;

import com.project.DomainRegistrationLive.dto.response.BlockResponse;
import com.project.DomainRegistrationLive.dto.response.DailySummaryResponse;
import com.project.DomainRegistrationLive.dto.response.SearchResponse;
import com.project.DomainRegistrationLive.exception.ErrorResponse;
import com.project.DomainRegistrationLive.service.LiveService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.Arrays;

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

    @GetMapping("/daily")
    public ResponseEntity<DailySummaryResponse> daily(@RequestParam String d) throws BadRequestException {
        Integer[] date = Arrays.stream(d.split("-")).map(Integer::parseInt).toArray(Integer[]::new);
        if(date.length != 3){
            throw new BadRequestException("Invalid date");
        }
        return ResponseEntity.ok().body(liveService.daily(LocalDate.of(date[0],date[1],date[2])));
    }

    @GetMapping("/block")
    public ResponseEntity<BlockResponse> block(@RequestParam(required = false, defaultValue = "0") String t) throws BadRequestException {

        return ResponseEntity.ok().body(liveService.block(Integer.parseInt(t)));
    }



}

