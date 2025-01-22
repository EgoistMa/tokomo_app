package org.tokomoapp.tokomoappbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tokomoapp.tokomoappbackend.model.Game;
import org.tokomoapp.tokomoappbackend.util.ExcelGameReader;
import org.tokomoapp.tokomoappbackend.repository.GameRepository;
import org.tokomoapp.tokomoappbackend.util.GameSearchUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.ArrayList;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;

    public List<Game> getGames() {
        try {
            return ExcelGameReader.readGamesFromExcel("src/main/resources/PC持续更新【1.10大更新】.xlsx");
        } catch (Exception e) {
            throw new RuntimeException("Failed to read games from Excel", e);
        }
    }

    public void saveGames(List<Game> games) {
        gameRepository.saveAll(games);
    }

    public List<Game> searchGames(String keyword) {
        List<Game> allGames = gameRepository.findAll();
        return GameSearchUtil.searchGames(allGames, keyword).stream()
            .map(Game::sanitize)
            .collect(Collectors.toList());
    }

    public Optional<Game> getGameById(String id) {
        return gameRepository.findById(id);
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    @Transactional
    public Game updateGame(String gameId, Game updates) {
        Game game = getGameById(gameId)
            .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));
        
        // 更新游戏信息
        if (updates.getGameName() != null) {
            game.setGameName(updates.getGameName());
        }
        if (updates.getGameType() != null) {
            game.setGameType(updates.getGameType());
        }
        if (updates.getDownloadUrl() != null) {
            game.setDownloadUrl(updates.getDownloadUrl());
        }
        if (updates.getPassword() != null) {
            game.setPassword(updates.getPassword());
        }
        if (updates.getExtractPassword() != null) {
            game.setExtractPassword(updates.getExtractPassword());
        }
        
        return gameRepository.save(game);
    }

    @Transactional
    public void overwriteGames(List<Game> newGames) {
        // 清空现有数据并保存新数据
        gameRepository.deleteAll();
        gameRepository.saveAll(newGames);
    }

    @Transactional
    public void mergeGames(List<Game> newGames) {
        // 获取所有现有游戏的ID
        Set<String> existingIds = gameRepository.findAll().stream()
            .map(Game::getId)
            .collect(Collectors.toSet());

        // 分类新游戏为更新和插入
        List<Game> toUpdate = new ArrayList<>();
        List<Game> toInsert = new ArrayList<>();

        for (Game game : newGames) {
            if (existingIds.contains(game.getId())) {
                toUpdate.add(game);
            } else {
                toInsert.add(game);
            }
        }

        // 更新现有游戏
        for (Game game : toUpdate) {
            Game existingGame = gameRepository.findById(game.getId()).get();
            existingGame.setGameName(game.getGameName());
            existingGame.setGameType(game.getGameType());
            existingGame.setDownloadUrl(game.getDownloadUrl());
            existingGame.setPassword(game.getPassword());
            existingGame.setExtractPassword(game.getExtractPassword());
            gameRepository.save(existingGame);
        }

        // 插入新游戏
        gameRepository.saveAll(toInsert);
    }
} 