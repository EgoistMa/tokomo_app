package org.tokomoapp.tokomo_be.service.impl;

import org.springframework.transaction.annotation.Propagation;
import org.tokomoapp.tokomo_be.model.Game;
import org.tokomoapp.tokomo_be.repository.GameRepository;
import org.tokomoapp.tokomo_be.repository.UserGameRepository;
import org.tokomoapp.tokomo_be.service.GameService;
import org.tokomoapp.tokomo_be.util.ExcelGameReader;
import org.tokomoapp.tokomo_be.util.GameSearchUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GameServiceImpl implements GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);
    
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserGameRepository userGameRepository;

    @Override
    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id);
    }
    @Override
    public List<Game> getGames() {
        try {
            return ExcelGameReader.readGamesFromExcel("src/main/resources/sampleData.xlsx");
        } catch (Exception e) {
            throw new RuntimeException("Failed to read games from Excel", e);
        }
    }

    public  void saveGame(Game game) {
        gameRepository.save(game);
    }

    @Override
    public void saveGames(List<Game> games) {
        gameRepository.saveAll(games);
    }

    @Override
    public List<Game> searchGames(String keyword) {
        List<Game> allGames = gameRepository.findAll();
        return GameSearchUtil.searchGames(allGames, keyword).stream()
            .map(Game::sanitize)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Game> getGameByGameName(String gameName) {
        return gameRepository.findByGameName(gameName);
    }

    @Override
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    @Transactional
    @Override
    public Game updateGame(Long gameId, Game updates) {
        Game game = getGameById(gameId)
            .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));
        
        // 更新所有非空字段
        if (updates.getGameName() != null) game.setGameName(updates.getGameName());
        if (updates.getGameType() != null) game.setGameType(updates.getGameType());
        if (updates.getDownloadUrl() != null) game.setDownloadUrl(updates.getDownloadUrl());
        if (updates.getPassword() != null) game.setPassword(updates.getPassword());
        if (updates.getExtractPassword() != null) game.setExtractPassword(updates.getExtractPassword());
        if (updates.getNote() != null) game.setNote(updates.getNote());
        
        return gameRepository.save(game);
    }

    @Override
    @Transactional
    public void deleteGame(Long gameId) {
        Game game = getGameById(gameId)
            .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));
        gameRepository.delete(game);
    }

    @Override
    @Transactional
    public void deleteAllGames() {
        List<Game> allGames = gameRepository.findAll();
        for (Game game : allGames) {
            if (!userGameRepository.existsByGameId(game.getId())) {
                gameRepository.delete(game);
            } else {
                logger.info("跳过删除游戏 [id={}，name={}]，因为存在用户购买记录", 
                    game.getId(), game.getGameName());
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Game> mergeGames(List<Game> newGames) {
        List<Game> mergedGames = new ArrayList<>();
        
        for (Game newGame : newGames) {
            try {
                Optional<Game> existingGame = gameRepository.findByGameName(newGame.getGameName());
                if (existingGame.isPresent()) {
                    existingGame.get().setGameType(newGame.getGameType());
                    existingGame.get().setDownloadUrl(newGame.getDownloadUrl());
                    existingGame.get().setPassword(newGame.getPassword());
                    existingGame.get().setExtractPassword(newGame.getExtractPassword());
                    existingGame.get().setNote(newGame.getNote());
                    gameRepository.save(existingGame.get());
                    mergedGames.add(existingGame.get());
                }else {
                    gameRepository.save(newGame);
                    mergedGames.add(newGame);
                }
            } catch (Exception e) {
                logger.error("处理游戏 [id={}，name={}] 时出错: {}", 
                    newGame.getId(), newGame.getGameName(), e.getMessage());
            }
        }
        
        return mergedGames;
    }
}

