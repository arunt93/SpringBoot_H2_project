package com.project.usermanagement.Service;

import com.project.usermanagement.Domain.User;
import com.project.usermanagement.Exception.DuplicateResourceException;
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
    @Transactional
    public User saveUser(User user) {
        log.debug("Saving user: {}", user.getEmail());
        
        validateUser(user);
        
        try {
            // Check for duplicate email (application-level check)
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new DuplicateResourceException("Email", user.getEmail());
            }
            
            // Check for duplicate username (application-level check)
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new DuplicateResourceException("Username", user.getUsername());
            }
            
            // Check for duplicate phone number (if provided)
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
                if (userRepository.existsByPhoneNumber(user.getPhoneNumber().trim())) {
                    throw new DuplicateResourceException("Phone number", user.getPhoneNumber().trim());
                }
            }
            
            User savedUser = userRepository.save(user);
            
            // Cache the newly saved user (handle Redis serialization gracefully)
            try {
                redisUserService.cacheUser(savedUser);
                redisUserService.cacheUserByEmail(savedUser.getEmail(), savedUser);
            } catch (Exception e) {
                log.warn("Failed to cache user in Redis: {}", e.getMessage());
                // Don't fail the operation if caching fails
            }
            
            log.info("User saved successfully with id: {}", savedUser.getId());
            return savedUser;
            
        } catch (Exception e) {
            // Handle database constraint violations
            if (e.getMessage() != null) {
                String message = e.getMessage().toLowerCase();
                if (message.contains("duplicate") || message.contains("unique")) {
                    if (message.contains("email")) {
                        throw new DuplicateResourceException("Email", user.getEmail());
                    } else if (message.contains("username")) {
                        throw new DuplicateResourceException("Username", user.getUsername());
                    } else if (message.contains("phone") || message.contains("phone_number")) {
                        throw new DuplicateResourceException("Phone number", user.getPhoneNumber());
                    } else {
                        throw new DuplicateResourceException("Resource", user.getEmail(), "Duplicate entry found. Please check your data.");
                    }
                }
            }
            // Re-throw other exceptions
            throw e;
        }
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
        if (user.getPhoneNumber() != null && user.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(null); // Convert empty string to null
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
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        return userRepository.findByUsername(username.trim())
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new RuntimeException("User not found with username: " + username);
                });
    }
    
    @Override
    @CacheEvict(value = {"users", "userByEmail"}, allEntries = true)
    @Transactional
    public User updateUser(Long id, User user) {
        log.debug("Updating user with id: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        
        validateUser(user);
        
        try {
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
            
            // Check if username is being changed to another existing username
            if (!existingUser.getUsername().equals(user.getUsername()) && 
                userRepository.existsByUsername(user.getUsername())) {
                throw new RuntimeException("Username already exists: " + user.getUsername());
            }
            
            // Check if phone number is being changed to another existing phone number
            String newPhone = user.getPhoneNumber();
            String existingPhone = existingUser.getPhoneNumber();
            if (newPhone != null && !newPhone.trim().isEmpty()) {
                newPhone = newPhone.trim();
                if (existingPhone == null || !existingPhone.equals(newPhone)) {
                    if (userRepository.existsByPhoneNumber(newPhone)) {
                        throw new RuntimeException("Phone number already exists: " + newPhone);
                    }
                }
            }
            
            // Update fields
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setEmail(user.getEmail());
            existingUser.setUsername(user.getUsername());
            existingUser.setPhoneNumber(newPhone);
            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                existingUser.setPassword(user.getPassword());
            }
            
            User updatedUser = userRepository.save(existingUser);
            
            // Update Redis cache (handle Redis serialization gracefully)
            try {
                redisUserService.cacheUser(updatedUser);
                redisUserService.cacheUserByEmail(updatedUser.getEmail(), updatedUser);
            } catch (Exception e) {
                log.warn("Failed to cache updated user in Redis: {}", e.getMessage());
                // Don't fail the operation if caching fails
            }
            
            log.info("User updated successfully with id: {}", updatedUser.getId());
            return updatedUser;
            
        } catch (Exception e) {
            // Handle database constraint violations
            if (e.getMessage() != null) {
                String message = e.getMessage().toLowerCase();
                if (message.contains("duplicate") || message.contains("unique")) {
                    if (message.contains("email")) {
                        throw new RuntimeException("Email already exists: " + user.getEmail());
                    } else if (message.contains("username")) {
                        throw new RuntimeException("Username already exists: " + user.getUsername());
                    } else if (message.contains("phone") || message.contains("phone_number")) {
                        throw new RuntimeException("Phone number already exists: " + user.getPhoneNumber());
                    } else {
                        throw new RuntimeException("Duplicate entry found. Please check your data.");
                    }
                }
            }
            // Re-throw other exceptions
            throw e;
        }
    }
}
