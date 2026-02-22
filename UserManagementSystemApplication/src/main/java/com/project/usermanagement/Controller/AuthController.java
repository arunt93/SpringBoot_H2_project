package com.project.usermanagement.Controller;

import com.project.usermanagement.DTO.ApiResponse;
import com.project.usermanagement.DTO.JwtAuthResponse;
import com.project.usermanagement.DTO.LoginRequest;
import com.project.usermanagement.DTO.RegisterRequest;
import com.project.usermanagement.Domain.User;
import com.project.usermanagement.Security.JwtTokenProvider;
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
            
            JwtAuthResponse authResponse = new JwtAuthResponse();
            authResponse.setAccessToken(jwt);
            authResponse.setUsername(loginRequest.getUsername());
            
            ApiResponse<JwtAuthResponse> response = ApiResponse.success("Login successful", authResponse);
            log.info("User logged in successfully: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), e);
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
}
