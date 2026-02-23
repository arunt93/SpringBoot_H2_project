package com.project.usermanagement.Controller;

import com.project.usermanagement.DTO.ApiResponse;
import com.project.usermanagement.DTO.JwtAuthResponse;
import com.project.usermanagement.DTO.LoginRequest;
import com.project.usermanagement.DTO.RegisterRequest;
import com.project.usermanagement.Domain.User;
import com.project.usermanagement.Security.JwtTokenProvider;
import com.project.usermanagement.Service.RedisUserService;
import com.project.usermanagement.Service.UserInterface;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserInterface userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisUserService redisUserService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.debug("Login attempt for user: {}", loginRequest.getUsername());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            
            // Store session in Redis
            User user = (User) authentication.getPrincipal();
            redisUserService.storeUserSession(jwt, user, Duration.ofHours(24));
            
            // Clear failed login attempts if any
            redisUserService.clearFailedAttempts(loginRequest.getUsername());
            
            JwtAuthResponse authResponse = new JwtAuthResponse();
            authResponse.setAccessToken(jwt);
            authResponse.setUsername(loginRequest.getUsername());
            
            ApiResponse<JwtAuthResponse> response = ApiResponse.success("Login successful", authResponse);
            log.info("User logged in successfully: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), e);
            
            // Increment failed login attempts
            redisUserService.incrementFailedAttempts(loginRequest.getUsername());
            
            // Check if account should be locked
            int attempts = redisUserService.getFailedAttempts(loginRequest.getUsername());
            if (attempts >= 5) {
                redisUserService.lockUserAccount(loginRequest.getUsername(), Duration.ofMinutes(30));
                log.warn("Account locked due to multiple failed attempts: {}", loginRequest.getUsername());
            }
            
            ApiResponse<JwtAuthResponse> response = ApiResponse.error("Invalid username or password", "AUTHENTICATION_FAILED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.debug("Registration attempt for user: {}", registerRequest.getUsername());
        
        try {
            if (userService.existsByUsername(registerRequest.getUsername())) {
                ApiResponse<User> response = ApiResponse.error("Username is already taken", "USERNAME_EXISTS");
                return ResponseEntity.badRequest().body(response);
            }

            if (userService.existsByEmail(registerRequest.getEmail())) {
                ApiResponse<User> response = ApiResponse.error("Email is already in use", "EMAIL_EXISTS");
                return ResponseEntity.badRequest().body(response);
            }

            User user = new User();
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setEmail(registerRequest.getEmail());
            user.setUsername(registerRequest.getUsername());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setPhoneNumber(registerRequest.getPhoneNumber());
            user.setIsActive(true);

            User savedUser = userService.saveUser(user);
            ApiResponse<User> response = ApiResponse.success("User registered successfully", savedUser);
            log.info("User registered successfully: {}", savedUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Registration failed for user: {}", registerRequest.getUsername(), e);
            ApiResponse<User> response = ApiResponse.error("Registration failed", "REGISTRATION_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser() {
        try {
            String token = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getCredentials() != null) {
                token = authentication.getCredentials().toString();
            }
            
            if (token != null) {
                // Remove session from Redis
                redisUserService.removeUserSession(token);
                
                // Blacklist the token
                redisUserService.blacklistToken(token, Duration.ofHours(24));
            }
            
            SecurityContextHolder.clearContext();
            
            ApiResponse<String> response = ApiResponse.success("Logout successful", "User logged out successfully");
            log.info("User logged out successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Logout failed", e);
            ApiResponse<String> response = ApiResponse.error("Logout failed", "LOGOUT_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
