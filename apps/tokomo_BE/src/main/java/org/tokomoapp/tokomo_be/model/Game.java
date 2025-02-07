package org.tokomoapp.tokomo_be.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "games")
public class Game {
    @Id
    @Column(name = "id", unique = true)
    private Long id;

    @Column(name = "game_type")
    private String gameType;

    @Column(name = "game_name", nullable = false, unique = true)
    private String gameName;

    @Column(name = "downloadUrl", nullable = false)
    private String downloadUrl;

    @Column(name = "password")
    private String password;

    @Column(name = "extract_password")
    private String extractPassword;

    @Column(name = "note")
    private String note;

    public Game sanitize() {
        Game sanitizedGame = new Game();
        sanitizedGame.setId(this.getId());
        sanitizedGame.setGameType(this.getGameType());
        sanitizedGame.setGameName(this.getGameName());
        return sanitizedGame;
    }
} 
