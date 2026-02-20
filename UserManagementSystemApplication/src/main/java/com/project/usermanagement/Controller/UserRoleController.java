package com.project.usermanagement.Controller;

import com.project.usermanagement.Domain.User;
import com.project.usermanagement.Domain.UserRoleMap;
import com.project.usermanagement.DTO.ApiResponse;
import com.project.usermanagement.Service.UserRoleMapService;
import com.project.usermanagement.Service.UserInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class UserRoleController {

    @Autowired
    private UserRoleMapService userRoleMapService;

    @Autowired
    private UserInterface userService;

    @PostMapping("/user-roles")
    public ResponseEntity<ApiResponse<UserRoleMap>> assignRole(@RequestBody Map<String, Object> request) {
        log.debug("Request received to assign role");
        try {
            Long userId = ((Number) request.get("userId")).longValue();
            String roleName = (String) request.get("roleName");
            Long assignedBy = request.get("assignedBy") != null ?
                    ((Number) request.get("assignedBy")).longValue() : null;

            User user = userService.getUserById(userId);
            UserRoleMap userRoleMap = userRoleMapService.assignRoleToUser(user, roleName, assignedBy);
            ApiResponse<UserRoleMap> response = ApiResponse.success("Role assigned successfully", userRoleMap);
            log.info("Role {} assigned to user {} successfully", roleName, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error assigning role", e);
            ApiResponse<UserRoleMap> response = ApiResponse.error("Failed to assign role", "ASSIGN_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/user-roles")
    public ResponseEntity<ApiResponse<Void>> removeRole(@RequestParam Long userId, @RequestParam String roleName) {
        log.debug("Request received to remove role {} from user {}", roleName, userId);
        try {
            userRoleMapService.removeRoleFromUser(userId, roleName);
            ApiResponse<Void> response = ApiResponse.success("Role removed successfully");
            log.info("Role {} removed from user {} successfully", roleName, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error removing role {} from user {}", roleName, userId, e);
            ApiResponse<Void> response = ApiResponse.error("Failed to remove role", "REMOVE_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user-roles")
    public ResponseEntity<ApiResponse<List<UserRoleMap>>> getUserRoles(@RequestParam Long userId) {
        log.debug("Request received to get roles for user {}", userId);
        try {
            List<UserRoleMap> userRoles = userRoleMapService.getUserRoles(userId);
            ApiResponse<List<UserRoleMap>> response = ApiResponse.success("User roles retrieved successfully", userRoles);
            log.info("Retrieved {} roles for user {}", userRoles.size(), userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching roles for user {}", userId, e);
            ApiResponse<List<UserRoleMap>> response = ApiResponse.error("Failed to fetch user roles", "FETCH_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user-roles/get-user-role-names")
    public ResponseEntity<ApiResponse<Set<String>>> getUserRoleNames(@RequestParam Long userId) {
        log.debug("Request received to get role names for user {}", userId);
        try {
            Set<String> roleNames = userRoleMapService.getUserRoleNames(userId);
            ApiResponse<Set<String>> response = ApiResponse.success("User role names retrieved successfully", roleNames);
            log.info("Retrieved {} role names for user {}", roleNames.size(), userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching role names for user {}", userId, e);
            ApiResponse<Set<String>> response = ApiResponse.error("Failed to fetch user role names", "FETCH_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user-roles/has-role")
    public ResponseEntity<ApiResponse<Boolean>> hasRole(@RequestParam Long userId, @RequestParam String roleName) {
        log.debug("Request received to check if user {} has role {}", userId, roleName);
        try {
            boolean hasRole = userRoleMapService.hasRole(userId, roleName);
            String message = hasRole ? "User has role" : "User does not have role";
            ApiResponse<Boolean> response = ApiResponse.success(message, hasRole);
            log.debug("User {} has role {}: {}", userId, roleName, hasRole);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking if user {} has role {}", userId, roleName, e);
            ApiResponse<Boolean> response = ApiResponse.error("Failed to check user role", "CHECK_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user-roles/get-role-users")
    public ResponseEntity<ApiResponse<List<UserRoleMap>>> getRoleUsers(@RequestParam Long roleId) {
        log.debug("Request received to get users for role {}", roleId);
        try {
            List<UserRoleMap> roleUsers = userRoleMapService.getRoleUsers(roleId);
            ApiResponse<List<UserRoleMap>> response = ApiResponse.success("Role users retrieved successfully", roleUsers);
            log.info("Retrieved {} users for role {}", roleUsers.size(), roleId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching users for role {}", roleId, e);
            ApiResponse<List<UserRoleMap>> response = ApiResponse.error("Failed to fetch role users", "FETCH_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
