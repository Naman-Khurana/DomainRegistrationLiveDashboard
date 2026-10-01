package com.project.DomainRegistrationLive.splitter;

import com.project.DomainRegistrationLive.splitter.dto.SplitItem;
import com.project.DomainRegistrationLive.splitter.dto.SplitRequest;
import com.project.DomainRegistrationLive.splitter.dto.SplitResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static com.project.DomainRegistrationLive.splitter.SplitConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SplitterClient {

    //splitterRestClient Bean
    private final RestClient http;

    public SplitResponse split(List<SplitItem> items){
        if(items == null || items.isEmpty()){
            return new SplitResponse(null, List.of());
        }
        long start = System.nanoTime();
        try {
            SplitResponse response = http.post()
                    .uri(SPLIT_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new SplitRequest(items))
                    .retrieve()
                    .body(SplitResponse.class);

            if(response == null || response.results().isEmpty()){
                throw new SplitterException(SPLITTER_INVALID_RESPONSE, "splitter service returned empty body");
            }

            log.debug("split {} domains in {} ms (model {})",
                    items.size(), (System.nanoTime() - start) / 1_000_000, response.modelVersion());

            return response;
        } catch (SplitterException e) {
            throw new SplitterException(SPLITTER_SERVICE_EXCEPTION,e.getMessage());
        }

    }

    public boolean isHealthy() {
        try {
            http.get().uri(HEALTH_CHECK_URL).retrieve().toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            log.warn("splitter health check failed: {}", e.getMessage());
            return false;
        }
    }




}
