package com.project.usermanagement.Service;

import com.project.usermanagement.Domain.Role;
import com.project.usermanagement.Domain.User;
import com.project.usermanagement.Domain.UserRoleMap;
import com.project.usermanagement.Repository.RoleRepository;
import com.project.usermanagement.Repository.UserRoleMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserRoleMapService {

    @Autowired
    private UserRoleMapRepository userRoleMapRepository;

    @Autowired
    private RoleRepository roleRepository;

    public UserRoleMap assignRoleToUser(User user, String roleName, Long assignedBy) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        UserRoleMap userRoleMap = new UserRoleMap();
        userRoleMap.setUser(user);
        userRoleMap.setRole(role);
        userRoleMap.setAssignedBy(assignedBy);
        userRoleMap.setIsActive(true);

        return userRoleMapRepository.save(userRoleMap);
    }

    public UserRoleMap assignRoleToUser(User user, Role role, Long assignedBy) {
        UserRoleMap userRoleMap = new UserRoleMap();
        userRoleMap.setUser(user);
        userRoleMap.setRole(role);
        userRoleMap.setAssignedBy(assignedBy);
        userRoleMap.setIsActive(true);

        return userRoleMapRepository.save(userRoleMap);
    }

    public void removeRoleFromUser(Long userId, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        userRoleMapRepository.deleteByUserIdAndRoleId(userId, role.getId());
    }

    public List<UserRoleMap> getUserRoles(Long userId) {
        return userRoleMapRepository.findByUserId(userId);
    }

    public Set<String> getUserRoleNames(Long userId) {
        return userRoleMapRepository.findRoleNamesByUserId(userId);
    }

    public boolean hasRole(Long userId, String roleName) {
        UserRoleMap userRoleMap = userRoleMapRepository.findByUserIdAndRoleName(userId, roleName);
        return userRoleMap != null;
    }

    public List<UserRoleMap> getRoleUsers(Long roleId) {
        return userRoleMapRepository.findByRoleId(roleId);
    }
}
