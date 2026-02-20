package com.project.usermanagement.Service;

import com.project.usermanagement.Entity.User;

import java.util.List;

public interface UserInterface {
      User getUserById(Long id);

      User saveUser(User user);

      List<User> getAllUser();

      void deleteUser(Long id);

}
