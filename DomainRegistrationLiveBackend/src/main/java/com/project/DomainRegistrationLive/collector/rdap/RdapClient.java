package com.project.DomainRegistrationLive.collector.rdap;

import com.project.DomainRegistrationLive.collector.CollectorProperties;
import com.project.DomainRegistrationLive.collector.dto.RdapResult;
import com.project.DomainRegistrationLive.collector.dto.RdapService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "collector", name = "enabled", havingValue = "true")
public class RdapClient {

    private final CollectorProperties collectorProperties;
    private final ObjectMapper objectMapper;



    private volatile Map<String, RdapService> servicesByTld = Map.of();
    private volatile long loadedAtMillis;
    private HttpClient http;

    private static final Duration DEFAULT_RETRY_AFTER = Duration.ofSeconds(30);
    private static final Duration MAX_RETRY_AFTER = Duration.ofMinutes(10);

    @PostConstruct
    private void init(){
        http = HttpClient.newBuilder()
                .connectTimeout(collectorProperties.httpTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public boolean isReady(){
        return !servicesByTld.isEmpty();
    }

    public void refreshIfStale(Duration maxAge){
        if(servicesByTld.isEmpty() || System.currentTimeMillis() - loadedAtMillis > maxAge.toMillis()){
            refreshBootStrap();
        }
    }

    boolean refreshBootStrap() {
        try {
            HttpRequest request = HttpRequest
                    .newBuilder(URI.create(collectorProperties.bootstrapUrl()))
                    .timeout(collectorProperties.httpTimeout())
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200){
                throw new IOException("HTTP" + response.statusCode());
            }

            Map<String, RdapService> map = new HashMap<>();
            for(JsonNode entry : objectMapper.readTree(response.body()).path("services")){
                String url = firstHttps(entry.get(1));
                if(url == null){
                    continue;
                }
                if(!url.endsWith("/")){
                    url = url + "/";
                }
                RdapService service = new RdapService(url, URI.create(url).getHost());
                for(JsonNode tld : entry.get(0)){
                    map.put(tld.asString().toLowerCase(Locale.ROOT), service );
                }
            }
            if(map.isEmpty()){
                throw new IOException("bootstrap file had no services");
            }

            //replace serviceByTld as a whole ( to prevent intermediate state abnormality due to multi threading)
            servicesByTld = Map.copyOf(map);
            loadedAtMillis = System.currentTimeMillis();
            log.info("RDAP bootstrap loaded: {} TLDs", map.size());
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException | RuntimeException e) {
            log.warn("RDAP bootstrap load failed: {}", e.toString());
            return false;
        }
    }

    private static String firstHttps(JsonNode urls) {
        for (JsonNode url : urls) {
            if (url.asString().startsWith("https://")) {
                return url.asString();
            }
        }
        return null;
    }

    public Optional<RdapService> serviceFor(String domain){
        int dot = domain.lastIndexOf(".");
        return dot < 0 ? Optional.empty() :
                Optional.ofNullable(servicesByTld.get(domain.substring(dot + 1)));
    }

    public RdapResult lookup(RdapService service, String domain) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(service.baseUrl() + "domain/" + domain))
                .timeout(collectorProperties.httpTimeout())
                .header("Accept", "application/rdap+json, application/json")
                .header("User-Agent", "DomainRegistrationLive/1.0")
                .GET().build();

        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            return switch (response.statusCode()) {
                case 200 -> parse(response.body());
                case 404 -> new RdapResult.NotFound();
                case 429 -> new RdapResult.RateLimited(retryAfter(response));
                default -> new RdapResult.Failed("HTTP " + response.statusCode());
            };
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new RdapResult.Failed("interrupted");
        } catch (IOException | RuntimeException e) {
            return new RdapResult.Failed(e.getClass().getSimpleName());
        }
    }

    private RdapResult parse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);

            String eventDate = null;
            for(JsonNode event : root.path("events")){
                if("registration".equals(event.path("eventAction").asString())){
                    eventDate = event.path("eventDate").asString(null);
                    break;
                }
            }
            if(eventDate == null){
                return new RdapResult.Failed("no registration event");
            }

            LocalDateTime registeredAt = OffsetDateTime.parse(eventDate)
                    .withOffsetSameInstant(
                            ZoneId.systemDefault().getRules().getOffset(Instant.now())
                    )
                    .toLocalDateTime();

            return new RdapResult.Confirmed(registeredAt, registrarIanaId(root));
        } catch (   DateTimeParseException e ) {
            return new RdapResult.Failed("unreadable response");
        }
        catch (Exception e){
            return new RdapResult.Failed("unreadable response");
        }
    }

    private static Integer registrarIanaId(JsonNode root) {
        for (JsonNode entity : root.path("entities")) {
            boolean isRegistrar = false;
            for (JsonNode role : entity.path("roles")) {
                if ("registrar".equalsIgnoreCase(role.asText())) {
                    isRegistrar = true;
                    break;
                }
            }
            if (!isRegistrar) {
                continue;
            }
            for (JsonNode id : entity.path("publicIds")) {
                if ("IANA Registrar ID".equalsIgnoreCase(id.path("type").asText())) {
                    try {
                        return Integer.valueOf(id.path("identifier").asText().trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private static Duration retryAfter(HttpResponse<?> response) {
        try {
            long seconds = Long.parseLong(response.headers().firstValue("Retry-After").orElse("").trim());
            Duration d = Duration.ofSeconds(Math.max(1, seconds));
            return d.compareTo(MAX_RETRY_AFTER) > 0 ? MAX_RETRY_AFTER : d;
        } catch (NumberFormatException e) {
            return DEFAULT_RETRY_AFTER;
        }
    }


}
