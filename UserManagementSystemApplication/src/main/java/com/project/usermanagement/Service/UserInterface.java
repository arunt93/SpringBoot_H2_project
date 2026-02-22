package com.project.usermanagement.Service;

import com.project.usermanagement.Domain.User;

import java.util.List;

public interface UserInterface {
      User getUserById(Long id);

      User saveUser(User user);

      List<User> getAllUser();

      void deleteUser(Long id);

      boolean existsByUsername(String username);
      
      boolean existsByEmail(String email);
      
      User updateUser(Long id, User user);

}
