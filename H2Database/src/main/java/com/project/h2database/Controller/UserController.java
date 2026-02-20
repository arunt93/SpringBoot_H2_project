package com.project.h2database.Controller;

import com.project.h2database.Entity.User;
import com.project.h2database.Service.UserInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserInterface userService;

    @PostMapping("/save")
    public ResponseEntity<?> saveUser(@RequestBody User user){
        try {
            User savedUser = userService.saveUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<User> getUserById(@RequestParam(name = "id") Long id){
        User user = userService.getUserById(id);
        if (user.getId() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users = userService.getAllUser();
        return ResponseEntity.ok(users);
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestParam Long id){
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
