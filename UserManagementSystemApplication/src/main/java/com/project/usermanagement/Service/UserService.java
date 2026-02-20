package com.project.usermanagement.Service;

import com.project.usermanagement.Domain.User;
import com.project.usermanagement.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class UserService implements UserInterface{

    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("UserService initialized");
    }
    
    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        log.debug("Attempting to find user with id: {}", id);
        
        if (id == null || id <= 0) {
            log.warn("Invalid user id provided: {}", id);
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        
        try {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                log.debug("User found with id: {}", id);
                return userOptional.get();
            } else {
                log.warn("User not found with id: {}", id);
                return null;
            }
        } catch (DataAccessException e) {
            log.error("Database error while fetching user with id: {}", id, e);
            throw new RuntimeException("Failed to fetch user from database", e);
        }
    }

    @Override
    public User saveUser(User user) {
        log.debug("Attempting to save user: {}", user.getEmail());
        
        if (user == null) {
            log.error("User object is null");
            throw new IllegalArgumentException("User object cannot be null");
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.error("User email is null or empty");
            throw new IllegalArgumentException("User email is required");
        }
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            log.error("User username is null or empty");
            throw new IllegalArgumentException("User username is required");
        }
        
        try {
            if (userRepository.existsByEmail(user.getEmail())) {
                log.warn("Email already exists: {}", user.getEmail());
                throw new RuntimeException("Email already exists: " + user.getEmail());
            }
            
            User savedUser = userRepository.save(user);
            log.info("User saved successfully with id: {} and email: {}", savedUser.getId(), savedUser.getEmail());
            return savedUser;
            
        } catch (DataAccessException e) {
            log.error("Database error while saving user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to save user to database", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUser() {
        log.debug("Fetching all users");
        
        try {
            List<User> users = userRepository.findAll();
            log.info("Successfully fetched {} users", users.size());
            return users;
        } catch (DataAccessException e) {
            log.error("Database error while fetching all users", e);
            throw new RuntimeException("Failed to fetch users from database", e);
        }
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Attempting to delete user with id: {}", id);
        
        if (id == null || id <= 0) {
            log.warn("Invalid user id provided for deletion: {}", id);
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        
        try {
            if (!userRepository.existsById(id)) {
                log.warn("User not found for deletion with id: {}", id);
                throw new RuntimeException("User not found with id: " + id);
            }
            
            userRepository.deleteById(id);
            log.info("User deleted successfully with id: {}", id);
            
        } catch (DataAccessException e) {
            log.error("Database error while deleting user with id: {}", id, e);
            throw new RuntimeException("Failed to delete user from database", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        log.debug("Checking if username exists: {}", username);
        
        if (username == null || username.trim().isEmpty()) {
            log.warn("Username is null or empty for existence check");
            return false;
        }
        
        try {
            boolean exists = userRepository.existsByUsername(username.trim());
            log.debug("Username {} exists: {}", username, exists);
            return exists;
        } catch (DataAccessException e) {
            log.error("Database error while checking username existence: {}", username, e);
            throw new RuntimeException("Failed to check username existence", e);
        }
    }
}
