package org.tokomoapp.tokomo_be.service;

import java.util.List;
import java.util.Optional;

import org.tokomoapp.tokomo_be.model.Game;

public interface GameService {
    Optional<Game> getGameById(Long gameId);
    List<Game> searchGames(String keyword);
    List<Game> getGames();
    void saveGame(Game game);
    void saveGames(List<Game> games);
    Optional<Game> getGameByGameName(String gameName);
    List<Game> getAllGames();
    Game updateGame(Long gameId, Game updates);
    void deleteGame(Long gameId);
    void deleteAllGames();
    List<Game> mergeGames(List<Game> newGames);

    Game mergeGameInNewTransaction(Game newGame);
}
