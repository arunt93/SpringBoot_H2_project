package com.project.usermanagement.Service;

import com.project.usermanagement.Domain.User;
import com.project.usermanagement.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        log.debug("Fetching user with id: {}", id);
        
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new RuntimeException("User not found with id: " + id);
                });
    }

    @Override
    public User saveUser(User user) {
        log.debug("Saving user: {}", user.getEmail());
        
        validateUser(user);
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        User savedUser = userRepository.save(user);
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
        
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        userRepository.deleteById(id);
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
        log.info("User updated successfully with id: {}", updatedUser.getId());
        return updatedUser;
    }
}
