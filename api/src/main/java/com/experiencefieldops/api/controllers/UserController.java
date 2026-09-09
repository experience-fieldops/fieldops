package com.experiencefieldops.api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.services.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @PostMapping
    public void saveUser(@RequestBody User user){
        userService.save(user);
    }

    @DeleteMapping("/{id}")
    public void deleteById(Long id){
        userService.deleteById(id);
    }

    @PutMapping("/{id}")
    public void updateUser(@RequestBody User user){
        userService.update(user);
    }
}
