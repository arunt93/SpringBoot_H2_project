package com.project.usermanagement.Service;

import com.project.usermanagement.Domain.User;
import com.project.usermanagement.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@Transactional
public class UserService implements UserInterface{

    private final UserRepository userRepository;
    private final RedisUserService redisUserService;

    @Autowired
    public UserService(UserRepository userRepository, RedisUserService redisUserService) {
        this.userRepository = userRepository;
        this.redisUserService = redisUserService;
        log.info("UserService initialized with Redis caching");
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        log.debug("Fetching user with id: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        
        // Try to get from Redis cache first
        User cachedUser = redisUserService.getCachedUser(id);
        if (cachedUser != null) {
            log.debug("User found in Redis cache: {}", id);
            return cachedUser;
        }
        
        // If not in cache, get from database
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new RuntimeException("User not found with id: " + id);
                });
        
        // Cache the user for future requests
        redisUserService.cacheUser(user);
        log.debug("User cached in Redis: {}", id);
        
        return user;
    }

    @Override
    @CacheEvict(value = {"users", "userByEmail"}, allEntries = true)
    public User saveUser(User user) {
        log.debug("Saving user: {}", user.getEmail());
        
        validateUser(user);
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        User savedUser = userRepository.save(user);
        
        // Cache the newly saved user
        redisUserService.cacheUser(savedUser);
        redisUserService.cacheUserByEmail(savedUser.getEmail(), savedUser);
        
        log.info("User saved successfully with id: {}", savedUser.getId());
        return savedUser;
    }
    
    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User object cannot be null");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email is required");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("User username is required");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUser() {
        log.debug("Fetching all users");
        
        List<User> users = userRepository.findAll();
        log.info("Fetched {} users", users.size());
        return users;
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        userRepository.deleteById(id);
        
        // Remove from Redis cache
        redisUserService.evictUserCache(id);
        redisUserService.evictUserCacheByEmail(user.getEmail());
        
        log.info("User deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        log.debug("Checking username existence: {}", username);
        
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        return userRepository.existsByUsername(username.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        log.debug("Checking email existence: {}", email);
        
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return userRepository.existsByEmail(email.trim());
    }
    
    @Override
    @CacheEvict(value = {"users", "userByEmail"}, allEntries = true)
    public User updateUser(Long id, User user) {
        log.debug("Updating user with id: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        
        validateUser(user);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for update with id: {}", id);
                    return new RuntimeException("User not found with id: " + id);
                });
        
        // Check if email is being changed to another existing email
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        // Update fields
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setUsername(user.getUsername());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setPassword(user.getPassword());
        
        User updatedUser = userRepository.save(existingUser);
        
        // Update Redis cache
        redisUserService.cacheUser(updatedUser);
        redisUserService.cacheUserByEmail(updatedUser.getEmail(), updatedUser);
        
        log.info("User updated successfully with id: {}", updatedUser.getId());
        return updatedUser;
    }
}
