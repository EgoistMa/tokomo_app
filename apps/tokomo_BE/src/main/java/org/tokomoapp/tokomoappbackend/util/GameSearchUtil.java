package org.tokomoapp.tokomoappbackend.util;

import org.tokomoapp.tokomoappbackend.model.Game;
import java.util.List;
import java.util.stream.Collectors;

public class GameSearchUtil {
    
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
} 