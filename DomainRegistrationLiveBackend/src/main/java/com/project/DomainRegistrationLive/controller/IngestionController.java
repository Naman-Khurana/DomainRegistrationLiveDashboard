package com.project.DomainRegistrationLive.controller;

import com.project.DomainRegistrationLive.dto.request.IngestRequest;
import com.project.DomainRegistrationLive.dto.response.IngestResponse;
import com.project.DomainRegistrationLive.service.IngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ingest")
@RequiredArgsConstructor
@Slf4j
public class IngestionController {

    private  final IngestionService ingestionService;

    public ResponseEntity<IngestResponse> ingest(@Valid @RequestBody  IngestRequest request){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ingestionService.ingest(request));
    }
}
