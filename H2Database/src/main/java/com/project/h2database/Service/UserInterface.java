package com.project.h2database.Service;

import com.project.h2database.Entity.User;

import java.util.List;

public interface UserInterface {
      User getUserById(Long id);

      User saveUser(User user);

      List<User> getAllUser();

      void deleteUser(Long id);

}
