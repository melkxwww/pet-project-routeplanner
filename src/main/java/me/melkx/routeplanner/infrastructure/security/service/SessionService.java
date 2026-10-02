package me.melkx.routeplanner.infrastructure.security.service;

import lombok.extern.slf4j.Slf4j;
import me.melkx.common.jwt.JwtGenerator;
import me.melkx.common.jwt.JwtParser;
import me.melkx.routeplanner.infrastructure.security.dto.JwtTokenPair;
import me.melkx.routeplanner.infrastructure.security.JwtValidityProperties;
import me.melkx.routeplanner.infrastructure.security.exception.InvalidRefreshTokenException;
import me.melkx.routeplanner.infrastructure.security.exception.RefreshTokenOwnershipException;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SessionService {
    private static final String REFRESH_PREFIX = "r:";
    private static final String USER_PREFIX = "u:";
    private static final String USER_SUFFIX = ":r";

    private static final DefaultRedisScript<Long> REMOVE_AND_CLEANUP_SCRIPT = new DefaultRedisScript<>(
            """
                    redis.call('SREM', KEYS[1], ARGV[1])
                    if redis.call('SCARD', KEYS[1]) == 0 then
                        redis.call('DEL', KEYS[1])
                    end
                    return 1
                    """,
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final JwtGenerator jwtGenerator;
    private final JwtParser jwtParser;
    private final JwtValidityProperties validityProperties;

    public SessionService(StringRedisTemplate redisTemplate,
                          JwtGenerator jwtGenerator,
                          JwtParser jwtParser,
                          JwtValidityProperties validityProperties) {
        this.redisTemplate = redisTemplate;
        this.jwtGenerator = jwtGenerator;
        this.jwtParser = jwtParser;
        this.validityProperties = validityProperties;
    }

    public JwtTokenPair newSession(UUID userId) {
        log.debug("Creating new session, userId={}", userId);

        RefreshTokenGenerationResult refresh = generateRefreshToken(userId);
        String accessToken = generateAccessToken(userId, refresh.jti());

        registerRefresh(userId, refresh.jti(), refresh.ttl());

        log.info("Session created, userId={}, jti={}, ttl={}s",
                userId, refresh.jti(), refresh.ttl().toSeconds());
        return new JwtTokenPair(accessToken, refresh.refreshToken());
    }

    public void terminateSession(String refreshToken, UUID userId) {
        log.debug("Terminating session, userId={}", userId);

        if (refreshToken == null) {
            log.warn("Terminate session failed: refresh token is null, userId={}", userId);
            throw new InvalidRefreshTokenException("Refresh token is required");
        }

        ToParseRefreshTokenPayload payload = jwtParser.parse(refreshToken, ToParseRefreshTokenPayload.class);

        if (!payload.sub().equals(userId)) {
            log.warn("Terminate session failed: ownership mismatch, authenticatedUserId={}, tokenUserId={}",
                    userId, payload.sub());
            throw new RefreshTokenOwnershipException();
        }

        revokeRefresh(payload.sub(), payload.jti());
        log.info("Session terminated, userId={}, jti={}", payload.sub(), payload.jti());
    }

    public void terminateAllSessions(UUID userId) {
        log.debug("Terminating all sessions, userId={}", userId);

        String userKey = prepareUserStorageKey(userId);
        Set<String> jtis = redisTemplate.opsForSet().members(userKey);
        if (jtis == null || jtis.isEmpty()) {
            redisTemplate.delete(userKey);
            log.info("No active sessions to terminate, userId={}", userId);
            return;
        }

        List<String> keysToDelete = jtis.stream()
                .map(jti -> prepareRefreshStorageKey(userId.toString(), jti))
                .collect(Collectors.toList());
        keysToDelete.add(userKey);

        redisTemplate.delete(keysToDelete);
        log.info("All sessions terminated, userId={}, count={}", userId, jtis.size());
    }

    public JwtTokenPair extendSession(String refreshToken) {
        log.debug("Extending session");

        if (refreshToken == null) {
            log.warn("Extend session failed: refresh token is null");
            throw new InvalidRefreshTokenException("Refresh token is required");
        }

        ToParseRefreshTokenPayload old = jwtParser.parse(refreshToken, ToParseRefreshTokenPayload.class);
        String oldKey = prepareRefreshStorageKey(old.sub(), old.jti());

        if (Boolean.FALSE.equals(redisTemplate.hasKey(oldKey))) {
            log.warn("Extend session failed: refresh token not found (revoked, expired, or reused), userId={}, jti={}",
                    old.sub(), old.jti());
            throw new InvalidRefreshTokenException("Refresh token is revoked or already used");
        }

        revokeRefresh(old.sub(), old.jti());
        log.debug("Old refresh token revoked, userId={}, jti={}", old.sub(), old.jti());

        RefreshTokenGenerationResult newRefresh = generateRefreshToken(old.sub());
        String newAccess = generateAccessToken(old.sub(), newRefresh.jti());
        registerRefresh(old.sub(), newRefresh.jti(), newRefresh.ttl());

        log.info("Session extended, userId={}, oldJti={}, newJti={}",
                old.sub(), old.jti(), newRefresh.jti());

        return new JwtTokenPair(newAccess, newRefresh.refreshToken());
    }

    private void registerRefresh(UUID userId, UUID jti, Duration ttl) {
        String refreshKey = prepareRefreshStorageKey(userId, jti);
        String userKey = prepareUserStorageKey(userId);

        log.trace("Registering refresh token, userId={}, jti={}, ttl={}s", userId, jti, ttl.toSeconds());

        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            @SuppressWarnings({"unchecked"})
            public Object execute(RedisOperations operations) {
                operations.opsForValue().set(refreshKey, "", ttl);
                operations.opsForSet().add(userKey, jti.toString());
                operations.expire(userKey, ttl);
                return null;
            }
        });
    }

    private void revokeRefresh(UUID userId, UUID jti) {
        String refreshKey = prepareRefreshStorageKey(userId, jti);
        String userKey = prepareUserStorageKey(userId);

        log.trace("Revoking refresh token, userId={}, jti={}", userId, jti);

        redisTemplate.delete(refreshKey);
        redisTemplate.execute(
                REMOVE_AND_CLEANUP_SCRIPT,
                List.of(userKey),
                jti.toString()
        );
    }

    private String generateAccessToken(UUID userId, UUID jti) {
        return jwtGenerator.generate(
                new ToGenerateAccessTokenPayload(userId, jti),
                Duration.ofSeconds(validityProperties.accessTokenValiditySeconds())
        );
    }

    private RefreshTokenGenerationResult generateRefreshToken(UUID userId) {
        UUID jti = UUID.randomUUID();
        Duration ttl = Duration.ofSeconds(validityProperties.refreshTokenValiditySeconds());

        String token = jwtGenerator.generate(
                new ToGenerateRefreshTokenPayload(userId, jti),
                ttl
        );

        return new RefreshTokenGenerationResult(token, jti, ttl);
    }

    private static String prepareRefreshStorageKey(UUID userId, UUID jti) {
        return prepareRefreshStorageKey(userId.toString(), jti.toString());
    }

    private static String prepareRefreshStorageKey(String userId, String jti) {
        return REFRESH_PREFIX + userId + ":" + jti;
    }

    private static String prepareUserStorageKey(UUID userId) {
        return prepareUserStorageKey(userId.toString());
    }

    private static String prepareUserStorageKey(String userId) {
        return USER_PREFIX + userId + USER_SUFFIX;
    }

    @EventListener
    public void onRefreshTokenExpired(RedisKeyExpiredEvent<Object> event) {
        Object source = event.getSource();
        if (source == null) {
            log.warn("Refresh token expired event with null source, skipping");
            return;
        }

        String expiredKey = source instanceof byte[] bytes
                ? new String(bytes, StandardCharsets.UTF_8)
                : source.toString();

        if (!expiredKey.startsWith(REFRESH_PREFIX)) return;

        String data = expiredKey.substring(REFRESH_PREFIX.length());
        int delimiterIndex = data.indexOf(':');
        if (delimiterIndex == -1) {
            log.warn("Malformed refresh key on expiry: {}", expiredKey);
            return;
        }

        String userId = data.substring(0, delimiterIndex);
        String jti = data.substring(delimiterIndex + 1);

        log.debug("Refresh token expired, cleaning up, userId={}, jti={}", userId, jti);

        redisTemplate.execute(
                REMOVE_AND_CLEANUP_SCRIPT,
                List.of(prepareUserStorageKey(userId)),
                jti
        );
    }

    record RefreshTokenGenerationResult(String refreshToken, UUID jti, Duration ttl) {
    }

    record ToParseRefreshTokenPayload(UUID sub, UUID jti, Instant exp) {
    }

    record ToGenerateRefreshTokenPayload(UUID sub, UUID jti) {
    }

    record ToGenerateAccessTokenPayload(UUID sub, UUID jti) {
    }
}