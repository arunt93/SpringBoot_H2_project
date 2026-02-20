package com.project.h2database.Service;

import com.project.h2database.Entity.User;
import com.project.h2database.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserInterface{

    @Autowired
    private UserRepository userRepository;
    
    @Override
    public User getUserById(Long id) {
        User emp = new User();
        Optional<User> byId = userRepository.findById(id);
        if(byId.isPresent()){
              emp = byId.get();
        }
        return emp;
    }

    @Override
    public User saveUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
