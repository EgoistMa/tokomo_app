package org.tokomoapp.tokomoappbackend.service;

import org.tokomoapp.tokomoappbackend.model.Game;
import java.util.List;
import java.util.Optional;

public interface GameService {
    List<Game> getGames();
    void saveGames(List<Game> games);
    List<Game> searchGames(String keyword);
    Optional<Game> getGameById(String id);
    List<Game> getAllGames();
    Game updateGame(String gameId, Game updates);
    void overwriteGames(List<Game> newGames);
    void mergeGames(List<Game> newGames);
}
