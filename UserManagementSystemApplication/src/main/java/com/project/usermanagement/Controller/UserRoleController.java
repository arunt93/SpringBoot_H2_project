package com.project.usermanagement.Controller;

import com.project.usermanagement.Domain.User;
import com.project.usermanagement.Domain.UserRoleMap;
import com.project.usermanagement.Service.UserRoleMapService;
import com.project.usermanagement.Service.UserInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/user-roles")
public class UserRoleController {
    
    @Autowired
    private UserRoleMapService userRoleMapService;
    
    @Autowired
    private UserInterface userService;
    
    @PostMapping("/assign")
    public ResponseEntity<UserRoleMap> assignRole(@RequestBody Map<String, Object> request) {
        Long userId = ((Number) request.get("userId")).longValue();
        String roleName = (String) request.get("roleName");
        Long assignedBy = request.get("assignedBy") != null ? 
                ((Number) request.get("assignedBy")).longValue() : null;
        
        User user = userService.getUserById(userId);
        UserRoleMap userRoleMap = userRoleMapService.assignRoleToUser(user, roleName, assignedBy);
        
        return ResponseEntity.ok(userRoleMap);
    }
    
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeRole(@RequestParam Long userId, @RequestParam String roleName) {
        userRoleMapService.removeRoleFromUser(userId, roleName);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserRoleMap>> getUserRoles(@PathVariable Long userId) {
        List<UserRoleMap> userRoles = userRoleMapService.getUserRoles(userId);
        return ResponseEntity.ok(userRoles);
    }
    
    @GetMapping("/user/{userId}/names")
    public ResponseEntity<Set<String>> getUserRoleNames(@PathVariable Long userId) {
        Set<String> roleNames = userRoleMapService.getUserRoleNames(userId);
        return ResponseEntity.ok(roleNames);
    }
    
    @GetMapping("/user/{userId}/has-role/{roleName}")
    public ResponseEntity<Boolean> hasRole(@PathVariable Long userId, @PathVariable String roleName) {
        boolean hasRole = userRoleMapService.hasRole(userId, roleName);
        return ResponseEntity.ok(hasRole);
    }
    
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<UserRoleMap>> getRoleUsers(@PathVariable Long roleId) {
        List<UserRoleMap> roleUsers = userRoleMapService.getRoleUsers(roleId);
        return ResponseEntity.ok(roleUsers);
    }
}
