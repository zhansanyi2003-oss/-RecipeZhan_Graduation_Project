package org.zhan.recipe_backend.service;

public interface AuthSessionRedisService {
    String createSession(Long userId);
    void deleteSession(Long userId);
    void refreshSession(Long userId, String sessionId);
    String getActiveSession(Long userId);
    boolean isActiveSession(Long userId, String sessionId);

}
