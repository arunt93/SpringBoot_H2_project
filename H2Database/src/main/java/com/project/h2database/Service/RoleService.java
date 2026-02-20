package com.project.h2database.Service;

import com.project.h2database.Entity.Role;
import com.project.h2database.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;
    
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }
    
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }
    
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }
    
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
    
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
}
