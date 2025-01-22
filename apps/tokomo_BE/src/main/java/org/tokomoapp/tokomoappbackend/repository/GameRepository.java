package org.tokomoapp.tokomoappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tokomoapp.tokomoappbackend.model.Game;

public interface GameRepository extends JpaRepository<Game, String> {
} 