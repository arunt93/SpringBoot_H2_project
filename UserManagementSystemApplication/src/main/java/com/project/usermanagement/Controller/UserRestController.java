package com.project.usermanagement.Controller;

import com.project.usermanagement.Domain.User;
import com.project.usermanagement.DTO.ApiResponse;
import com.project.usermanagement.Service.UserInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class UserRestController {
    
    private final UserInterface userService;

    @Autowired
    public UserRestController(UserInterface userService) {
        this.userService = userService;
        log.info("UserRestController initialized");
    }

    // POST /api/v1/users - Create new user
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        log.debug("Request received to create user: {}", user.getEmail());
        
        try {
            User savedUser = userService.saveUser(user);
            ApiResponse<User> response = ApiResponse.success("User created successfully", savedUser);
            log.info("User created successfully with id: {}", savedUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid input for user creation: {}", e.getMessage());
            ApiResponse<User> response = ApiResponse.error("Validation error: " + e.getMessage(), "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(response);
            
        } catch (RuntimeException e) {
            log.error("Error creating user: {}", e.getMessage());
            ApiResponse<User> response = ApiResponse.error(e.getMessage(), "USER_CREATION_ERROR");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // GET /api/v1/users - Get all users
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        log.debug("Request received to get all users");
        
        try {
            List<User> users = userService.getAllUser();
            ApiResponse<List<User>> response = ApiResponse.success("Users retrieved successfully", users);
            log.info("Retrieved {} users", users.size());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Error fetching all users", e);
            ApiResponse<List<User>> response = ApiResponse.error("Failed to fetch users", "FETCH_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // GET /api/v1/users/get-by-id - Get user by ID
    @GetMapping("/users/get-by-id")
    public ResponseEntity<ApiResponse<User>> getUserById(@RequestParam Long id) {
        log.debug("Request received to get user by id: {}", id);
        
        try {
            User user = userService.getUserById(id);
            ApiResponse<User> response = ApiResponse.success("User found", user);
            log.info("User found with id: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Error fetching user with id: {}", id, e);
            ApiResponse<User> response = ApiResponse.error(e.getMessage(), "FETCH_ERROR");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // PUT /api/v1/users/update - Update user
    @PutMapping("/users/update")
    public ResponseEntity<ApiResponse<User>> updateUser(@RequestParam Long id, @RequestBody User user) {
        log.debug("Request received to update user with id: {}", id);
        
        try {
            User updatedUser = userService.updateUser(id, user);
            ApiResponse<User> response = ApiResponse.success("User updated successfully", updatedUser);
            log.info("User updated successfully with id: {}", updatedUser.getId());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid input for user update: {}", e.getMessage());
            ApiResponse<User> response = ApiResponse.error("Validation error: " + e.getMessage(), "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(response);
            
        } catch (RuntimeException e) {
            log.error("Error updating user with id: {}", id, e);
            ApiResponse<User> response = ApiResponse.error(e.getMessage(), "UPDATE_ERROR");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // DELETE /api/v1/users/delete - Delete user
    @DeleteMapping("/users/delete")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@RequestParam Long id) {
        log.debug("Request received to delete user with id: {}", id);
        
        try {
            userService.deleteUser(id);
            ApiResponse<Void> response = ApiResponse.success("User deleted successfully");
            log.info("User deleted successfully with id: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user id provided for deletion: {}", e.getMessage());
            ApiResponse<Void> response = ApiResponse.error("Invalid input: " + e.getMessage(), "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(response);
            
        } catch (RuntimeException e) {
            log.error("Error deleting user with id: {}", id, e);
            ApiResponse<Void> response = ApiResponse.error(e.getMessage(), "DELETE_ERROR");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // GET /api/v1/users/exists-by-username - Check username existence
    @GetMapping("/users/exists-by-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUserExistsByUsername(@RequestParam String username) {
        log.debug("Request received to check username existence: {}", username);
        
        try {
            boolean exists = userService.existsByUsername(username);
            String message = exists ? "Username exists" : "Username does not exist";
            ApiResponse<Boolean> response = ApiResponse.success(message, exists);
            log.debug("Username {} exists: {}", username, exists);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Error checking username existence: {}", username, e);
            ApiResponse<Boolean> response = ApiResponse.error("Failed to check username", "CHECK_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
