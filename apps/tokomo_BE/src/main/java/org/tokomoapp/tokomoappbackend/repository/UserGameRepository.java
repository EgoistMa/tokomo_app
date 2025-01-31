package org.tokomoapp.tokomoappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tokomoapp.tokomoappbackend.model.UserGame;
import java.util.List;
import java.util.Optional;

public interface UserGameRepository extends JpaRepository<UserGame, Long> {
    List<UserGame> findByUserId(Long userId);
    Optional<UserGame> findByUserIdAndGameId(Long userId, String gameId);
    boolean existsByUserIdAndGameId(Long userId, String gameId);
} 