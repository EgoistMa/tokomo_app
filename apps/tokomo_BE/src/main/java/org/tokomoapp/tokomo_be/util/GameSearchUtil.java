package org.tokomoapp.tokomo_be.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.tokomoapp.tokomo_be.model.Game;
import org.tokomoapp.tokomo_be.service.GameService;

import java.util.List;
import java.util.stream.Collectors;

public class GameSearchUtil {

    @Autowired
    private GameService gameService;
    
    public static List<Game> searchGames(List<Game> games, String keyword) {
        if (keyword == null) {
            return games;
        }
        
        return games.stream()
            .filter(game -> {
                // 检查游戏名称是否包含关键字
                if (game.getGameName() != null && 
                    game.getGameName().toLowerCase().contains(keyword.toLowerCase())) {
                    return true;
                }
                return false;
            })
            .collect(Collectors.toList());
    }

    public void LoadGames() {
        List<Game> games = gameService.getGames();
        gameService.saveGames(games);
    }
} 