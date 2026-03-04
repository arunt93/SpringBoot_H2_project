package com.project.usermanagement.Service;

import com.project.usermanagement.Domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisUserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String USER_CACHE_PREFIX = "user:";
    private static final String USER_EMAIL_PREFIX = "user:email:";
    private static final String USER_SESSION_PREFIX = "session:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // Cache user by ID
    public void cacheUser(User user) {
        try {
            String key = USER_CACHE_PREFIX + user.getId();
            redisTemplate.opsForValue().set(key, user, Duration.ofHours(1));
        } catch (Exception e) {
            // Log but don't fail the operation
            System.err.println("Failed to cache user by ID: " + e.getMessage());
        }
    }

    // Get cached user by ID
    public User getCachedUser(Long userId) {
        String key = USER_CACHE_PREFIX + userId;
        return (User) redisTemplate.opsForValue().get(key);
    }

    // Cache user by email
    public void cacheUserByEmail(String email, User user) {
        try {
            String key = USER_EMAIL_PREFIX + email;
            redisTemplate.opsForValue().set(key, user, Duration.ofHours(1));
        } catch (Exception e) {
            // Log but don't fail the operation
            System.err.println("Failed to cache user by email: " + e.getMessage());
        }
    }

    // Get cached user by email
    public User getCachedUserByEmail(String email) {
        String key = USER_EMAIL_PREFIX + email;
        return (User) redisTemplate.opsForValue().get(key);
    }

    // Remove user from cache
    public void evictUserCache(Long userId) {
        redisTemplate.delete(USER_CACHE_PREFIX + userId);
    }

    // Remove user from cache by email
    public void evictUserCacheByEmail(String email) {
        redisTemplate.delete(USER_EMAIL_PREFIX + email);
    }

    // Store user session
    public void storeUserSession(String token, User user, Duration duration) {
        String key = USER_SESSION_PREFIX + token;
        redisTemplate.opsForValue().set(key, user, duration);
    }

    // Get user session
    public User getUserSession(String token) {
        String key = USER_SESSION_PREFIX + token;
        return (User) redisTemplate.opsForValue().get(key);
    }

    // Remove user session (logout)
    public void removeUserSession(String token) {
        redisTemplate.delete(USER_SESSION_PREFIX + token);
    }

    // Add JWT token to blacklist
    public void blacklistToken(String token, Duration duration) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", duration);
    }

    // Check if token is blacklisted
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }

    // Cache user count
    public void cacheUserCount(Long count) {
        redisTemplate.opsForValue().set("user:count", count, Duration.ofMinutes(10));
    }

    // Get cached user count
    public Long getCachedUserCount() {
        Object count = redisTemplate.opsForValue().get("user:count");
        return count != null ? (Long) count : null;
    }

    // Increment failed login attempts
    public void incrementFailedAttempts(String email) {
        String key = "login:attempts:" + email;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(15));
    }

    // Get failed login attempts
    public int getFailedAttempts(String email) {
        String key = "login:attempts:" + email;
        Object attempts = redisTemplate.opsForValue().get(key);
        return attempts != null ? (Integer) attempts : 0;
    }

    // Clear failed login attempts
    public void clearFailedAttempts(String email) {
        String key = "login:attempts:" + email;
        redisTemplate.delete(key);
    }

    // Lock user account temporarily
    public void lockUserAccount(String email, Duration duration) {
        String key = "account:locked:" + email;
        redisTemplate.opsForValue().set(key, "locked", duration);
    }

    // Check if user account is locked
    public boolean isAccountLocked(String email) {
        String key = "account:locked:" + email;
        return redisTemplate.hasKey(key);
    }

    // Cache user permissions
    public void cacheUserPermissions(Long userId, String permissions, Duration duration) {
        String key = "user:permissions:" + userId;
        redisTemplate.opsForValue().set(key, permissions, duration);
    }

    // Get cached user permissions
    public String getCachedUserPermissions(Long userId) {
        String key = "user:permissions:" + userId;
        return (String) redisTemplate.opsForValue().get(key);
    }
}
