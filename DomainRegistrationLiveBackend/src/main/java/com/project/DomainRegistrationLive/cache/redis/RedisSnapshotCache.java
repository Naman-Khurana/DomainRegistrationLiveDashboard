package com.project.DomainRegistrationLive.cache.redis;

import com.project.DomainRegistrationLive.cache.SnapshotCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

import static com.project.DomainRegistrationLive.dto.SnapshotModels.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSnapshotCache implements SnapshotCache {

    private static final String PREFIX = "snapshot:";
    private static final Duration TTL = Duration.ofSeconds(90);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<SnapshotResponse> get(String keyId) {
        try {
            String json =stringRedisTemplate.opsForValue().get( PREFIX + keyId );
            if(json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, SnapshotResponse.class));
        } catch (Exception e) {
            log.warn("Snapshot cache failed, keyId: {}" , keyId);
            return Optional.empty();

        }
    }

    @Override
    public void put(String keyId, SnapshotResponse snapshot) {
        try {
            stringRedisTemplate.opsForValue().set(PREFIX + keyId,
                    objectMapper.writeValueAsString(snapshot),
                    TTL);
        } catch (Exception e) {
            log.warn("Snapshot cache put failed, keyId: {}" , keyId);
        }
    }

    @Override
    public void evict(String keyId) {
        try {
            stringRedisTemplate.delete(PREFIX + keyId);
        } catch (Exception e) {
            log.warn("Snapshot cache evict failed, keyId: {}" , keyId);
        }
    }
}
