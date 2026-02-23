package com.project.usermanagement.Controller;

import com.project.usermanagement.Domain.User;
import com.project.usermanagement.Service.RedisUserService;
import com.project.usermanagement.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/redis")
public class RedisUserController {

    @Autowired
    private RedisUserService redisUserService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testRedisConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test basic Redis operations
            String testKey = "test:connection:" + System.currentTimeMillis();
            String testValue = "Redis test at " + System.currentTimeMillis();
            
            redisUserService.cacheUserCount(1L);
            Long cachedCount = redisUserService.getCachedUserCount();
            
            response.put("status", "success");
            response.put("message", "Redis connection is working");
            response.put("cached_count", cachedCount);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Redis connection failed");
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Map<String, Object>> getUserWithCache(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            User user = userService.getUserById(id);
            long endTime = System.currentTimeMillis();
            
            response.put("status", "success");
            response.put("user", user);
            response.put("response_time_ms", endTime - startTime);
            response.put("message", "User retrieved with Redis caching");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to get user");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(404).body(response);
        }
    }

    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> createSession(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = (String) request.get("token");
            Long userId = Long.valueOf(request.get("userId").toString());
            
            User user = userService.getUserById(userId);
            redisUserService.storeUserSession(token, user, Duration.ofHours(1));
            
            response.put("status", "success");
            response.put("message", "Session created successfully");
            response.put("token", token);
            response.put("user_id", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create session");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/session/{token}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = redisUserService.getUserSession(token);
            
            if (user != null) {
                response.put("status", "success");
                response.put("user", user);
                response.put("message", "Session found");
            } else {
                response.put("status", "not_found");
                response.put("message", "Session not found or expired");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to get session");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/session/{token}")
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            redisUserService.removeUserSession(token);
            
            response.put("status", "success");
            response.put("message", "Session deleted successfully");
            response.put("token", token);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete session");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/blacklist")
    public ResponseEntity<Map<String, Object>> blacklistToken(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = (String) request.get("token");
            redisUserService.blacklistToken(token, Duration.ofHours(24));
            
            response.put("status", "success");
            response.put("message", "Token blacklisted successfully");
            response.put("token", token);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to blacklist token");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/blacklist/{token}")
    public ResponseEntity<Map<String, Object>> checkBlacklist(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isBlacklisted = redisUserService.isTokenBlacklisted(token);
            
            response.put("status", "success");
            response.put("token", token);
            response.put("is_blacklisted", isBlacklisted);
            response.put("message", isBlacklisted ? "Token is blacklisted" : "Token is not blacklisted");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to check blacklist");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/login-attempts")
    public ResponseEntity<Map<String, Object>> incrementLoginAttempts(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = (String) request.get("email");
            redisUserService.incrementFailedAttempts(email);
            
            int attempts = redisUserService.getFailedAttempts(email);
            
            response.put("status", "success");
            response.put("email", email);
            response.put("failed_attempts", attempts);
            response.put("message", "Login attempts incremented");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to increment login attempts");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/login-attempts/{email}")
    public ResponseEntity<Map<String, Object>> getLoginAttempts(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int attempts = redisUserService.getFailedAttempts(email);
            boolean isLocked = redisUserService.isAccountLocked(email);
            
            response.put("status", "success");
            response.put("email", email);
            response.put("failed_attempts", attempts);
            response.put("is_locked", isLocked);
            response.put("message", "Login attempts retrieved");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to get login attempts");
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
