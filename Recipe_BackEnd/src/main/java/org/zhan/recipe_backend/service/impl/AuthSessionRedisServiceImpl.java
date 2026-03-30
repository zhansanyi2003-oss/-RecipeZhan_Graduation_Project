package org.zhan.recipe_backend.service.impl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.zhan.recipe_backend.service.AuthSessionRedisService;

import java.time.Duration;
import java.util.UUID;

@Service
public class AuthSessionRedisServiceImpl implements AuthSessionRedisService {

    private static final Duration SESSION_TTL = Duration.ofHours(24);
    private static final String SESSION_KEY_PREFIX = "auth:active_session:";

    private final StringRedisTemplate stringRedisTemplate;
    public AuthSessionRedisServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    public String createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(buildKey(userId), sessionId, SESSION_TTL);
        return sessionId;
    }

    public void refreshSession(Long userId, String sessionId) {
        stringRedisTemplate.opsForValue().set(buildKey(userId), sessionId, SESSION_TTL);
    }

    public String getActiveSession(Long userId) {
        return stringRedisTemplate.opsForValue().get(buildKey(userId));
    }

    public boolean isActiveSession(Long userId, String sessionId) {
        if (userId == null || sessionId == null || sessionId.isBlank()) {
            return false;
        }
        String activeSessionId = getActiveSession(userId);
        return sessionId.equals(activeSessionId);
    }

    public void deleteSession(Long userId) {
        stringRedisTemplate.delete(buildKey(userId));
    }

    private String buildKey(Long userId) {
        return SESSION_KEY_PREFIX + userId;
    }
}
