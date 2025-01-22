package org.tokomoapp.tokomoappbackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "games")
public class Game {
    @Id
    private String id;
    private String gameType;
    private String gameName;
    private String downloadUrl;
    private String password;
    private String extractPassword;

    public Game sanitize() {
        Game sanitizedGame = new Game();
        sanitizedGame.setId(this.getId());
        sanitizedGame.setGameType(this.getGameType());
        sanitizedGame.setGameName(this.getGameName());
        return sanitizedGame;
    }
} 
