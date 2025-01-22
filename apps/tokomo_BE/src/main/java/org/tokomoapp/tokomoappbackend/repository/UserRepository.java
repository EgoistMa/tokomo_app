package org.tokomoapp.tokomoappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tokomoapp.tokomoappbackend.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
} 
