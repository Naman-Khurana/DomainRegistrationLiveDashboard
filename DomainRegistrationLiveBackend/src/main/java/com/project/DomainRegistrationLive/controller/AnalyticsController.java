//package com.project.DomainRegistrationLive.controller;
//
//import com.project.DomainRegistrationLive.dto.*;
//import com.project.DomainRegistrationLive.dto.projection.KeywordAnalyticsProjection;
//import com.project.DomainRegistrationLive.service.AnalyticsService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/v1/analytics")
//public class AnalyticsController {
//
//    private final AnalyticsService analyticsService;
//
//    @GetMapping()
//    public DomainStatsProjection getDomainStats() {
//        return analyticsService.getDomainStats();
//    }
//
//    @GetMapping("/tld-counts")
//    public List<TldCountProjection> getTldCounts() {
//        return analyticsService.getTldCounts();
//    }
//
//    @GetMapping("/registrar-counts")
//    public List<RegistrarCountProjection> getRegistrarCounts() {
//        return analyticsService.getRegistrarCounts();
//    }
//
//    @GetMapping("/keyword-counts")
//    public List<KeywordCountProjection> getKeywordCounts() {
//        return analyticsService.getKeywordCounts();
//    }
//
//    @GetMapping("/prefix-counts")
//    public List<PrefixCountProjection> getPrefixCounts() {
//        return analyticsService.getPrefixCounts();
//    }
//
//    @GetMapping("/suffix-counts")
//    public List<SuffixCountProjection> getSuffixCounts() {
//        return analyticsService.getSuffixCounts();
//    }
//
//    @GetMapping("/domain-format")
//    public DomainFormatProjection getDomainFormat() {
//        return analyticsService.getDomainFormat();
//    }
//
//
//    @GetMapping("/repeated-slds")
//    public List<RepeatedSldProjection> getRepeatedSlds() {
//        return analyticsService.getRepeatedSlds();
//    }
//
//    @GetMapping("/keyword-analytics")
//    public KeywordAnalyticsProjection getKeywordAnalytics() {
//        return analyticsService.getKeywordAnalytics();
//    }
//
//    @GetMapping("/rising-keywords")
//    public RisingKeywordAnalytics getRisingKeywords() {
//        return analyticsService.getRisingKeywords();
//    }
//
//    @GetMapping("/feed")
//    public FeedResponse getFeed(
//            @RequestParam(required = false) LocalDateTime since
//    ) {
//        return analyticsService.getFeed(since);
//    }
//}
//
