package com.project.DomainRegistrationLive.worker;

import com.project.DomainRegistrationLive.config.SegmentationProperties;
import com.project.DomainRegistrationLive.service.SegmentationService;
import com.project.DomainRegistrationLive.splitter.SplitterException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SegmentationWorker {

    private volatile boolean running;
    private Thread thread;
    private final SegmentationProperties segmentationProperties;
    private final SegmentationService segmentationService;

    @EventListener(ApplicationReadyEvent.class)
    public void start(){
        if(running){
            return;
        }

        running = true;
        thread = new Thread(this::processPendingDomains, "segmentation-worker");
        thread.setDaemon(true);
        thread.start();
        log.info("segmentation worker started");
    }

    @PreDestroy
    public void stop(){
        running = false;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processPendingDomains() {
        while(running){
            try {
                int domainsProcessed = segmentationService.processBatch();
                if(domainsProcessed == 0 ){

                    long sleepMs = segmentationProperties.idleSleep().toMillis();

                    log.info("No pending domains. Sleeping for {} ms", sleepMs);

                    if (!sleep(sleepMs)) {
                        break;
                    }
                }
            }catch (SplitterException e) {
                log.warn("splitter unavailable, retrying later: {}", e.getMessage());
                if (!sleep(segmentationProperties.errorBackoff().toMillis())) {
                    break;
                }
            }
            catch (Exception e) {
                log.error("segmentation batch failed", e);
                if (!sleep(segmentationProperties.errorBackoff().toMillis())) {
                    break;
                }
            }
        }

        log.info("segmentation worker stopped");
    }



    private boolean sleep(long ms) {
        try {
            Thread.sleep(ms);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }


}
