package com.project.usermanagement.Controller;

import com.project.usermanagement.Domain.Role;
import com.project.usermanagement.DTO.ApiResponse;
import com.project.usermanagement.Service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<Role>> saveRole(@RequestBody Role role) {
        log.debug("Request received to save role: {}", role.getName());
        try {
            if (roleService.existsByName(role.getName())) {
                ApiResponse<Role> response = ApiResponse.error("Role already exists: " + role.getName(), "ROLE_EXISTS_ERROR");
                return ResponseEntity.badRequest().body(response);
            }
            Role savedRole = roleService.saveRole(role);
            ApiResponse<Role> response = ApiResponse.success("Role created successfully", savedRole);
            log.info("Role created successfully with id: {}", savedRole.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating role: {}", role.getName(), e);
            ApiResponse<Role> response = ApiResponse.error("Failed to create role", "ROLE_CREATION_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/roles/get-by-id")
    public ResponseEntity<ApiResponse<Role>> getRoleById(@RequestParam Long id) {
        log.debug("Request received to get role by id: {}", id);
        try {
            Optional<Role> role = roleService.getRoleById(id);
            if (role.isPresent()) {
                ApiResponse<Role> response = ApiResponse.success("Role found", role.get());
                log.info("Role found with id: {}", id);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<Role> response = ApiResponse.error("Role not found with id: " + id, "ROLE_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("Error fetching role with id: {}", id, e);
            ApiResponse<Role> response = ApiResponse.error("Failed to fetch role", "FETCH_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/roles/get-by-name")
    public ResponseEntity<ApiResponse<Role>> getRoleByName(@RequestParam String name) {
        log.debug("Request received to get role by name: {}", name);
        try {
            Optional<Role> role = roleService.getRoleByName(name);
            if (role.isPresent()) {
                ApiResponse<Role> response = ApiResponse.success("Role found", role.get());
                log.info("Role found with name: {}", name);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<Role> response = ApiResponse.error("Role not found with name: " + name, "ROLE_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("Error fetching role with name: {}", name, e);
            ApiResponse<Role> response = ApiResponse.error("Failed to fetch role", "FETCH_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        log.debug("Request received to get all roles");
        try {
            List<Role> roles = roleService.getAllRoles();
            ApiResponse<List<Role>> response = ApiResponse.success("Roles retrieved successfully", roles);
            log.info("Retrieved {} roles", roles.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching all roles", e);
            ApiResponse<List<Role>> response = ApiResponse.error("Failed to fetch roles", "FETCH_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/roles/delete")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@RequestParam Long id) {
        log.debug("Request received to delete role with id: {}", id);
        try {
            roleService.deleteRole(id);
            ApiResponse<Void> response = ApiResponse.success("Role deleted successfully");
            log.info("Role deleted successfully with id: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting role with id: {}", id, e);
            ApiResponse<Void> response = ApiResponse.error("Failed to delete role", "DELETE_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
