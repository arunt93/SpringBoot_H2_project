package com.project.h2database.Repository;

import com.project.h2database.Entity.UserRoleMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRoleMapRepository extends JpaRepository<UserRoleMap, Long> {
    List<UserRoleMap> findByUserId(Long userId);
    List<UserRoleMap> findByRoleId(Long roleId);
    
    @Query("SELECT urm.role.name FROM UserRoleMap urm WHERE urm.user.id = :userId AND urm.isActive = true")
    Set<String> findRoleNamesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT urm FROM UserRoleMap urm WHERE urm.user.id = :userId AND urm.role.name = :roleName AND urm.isActive = true")
    UserRoleMap findByUserIdAndRoleName(@Param("userId") Long userId, @Param("roleName") String roleName);
    
    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
