package com.experiencefieldops.api.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.repositories.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteById(Long id){
        userRepository.deleteById(id);
    }

    public User update(User user){
        if(user.getId() == null || !userRepository.existsById(user.getId())){
            return null;
        }
        return userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
}
