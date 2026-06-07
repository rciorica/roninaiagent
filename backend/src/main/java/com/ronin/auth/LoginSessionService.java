package com.ronin.auth;

import com.ronin.users.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage temporary login sessions during two-step authentication
 */
@Service
public class LoginSessionService {

    private static final long SESSION_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes
    private final Map<String, LoginSession> sessions = new ConcurrentHashMap<>();

    /**
     * Create a session after email verification
     * @return Session ID
     */
    public String createSession(UserEntity user) {
        String sessionId = UUID.randomUUID().toString();
        LoginSession session = new LoginSession(user, System.currentTimeMillis());
        sessions.put(sessionId, session);
        return sessionId;
    }

    /**
     * Get user from session and validate timeout
     */
    public UserEntity getUserFromSession(String sessionId) {
        LoginSession session = sessions.get(sessionId);
        
        if (session == null) {
            throw new RuntimeException("Invalid or expired session");
        }
        
        if (System.currentTimeMillis() - session.createdAt > SESSION_TIMEOUT_MS) {
            sessions.remove(sessionId);
            throw new RuntimeException("Session expired");
        }
        
        return session.user;
    }

    /**
     * Invalidate session after successful login
     */
    public void invalidateSession(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * Clean up expired sessions (can be called periodically)
     */
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> 
            now - entry.getValue().createdAt > SESSION_TIMEOUT_MS
        );
    }

    @Data
    @AllArgsConstructor
    private static class LoginSession {
        private UserEntity user;
        private long createdAt;
    }
}
