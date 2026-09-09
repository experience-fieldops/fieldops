package com.experiencefieldops.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.User;

public interface UserRepository extends JpaRepository<User, Long>{
    
}
