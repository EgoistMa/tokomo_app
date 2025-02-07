package org.tokomoapp.tokomo_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tokomoapp.tokomo_be.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
} 