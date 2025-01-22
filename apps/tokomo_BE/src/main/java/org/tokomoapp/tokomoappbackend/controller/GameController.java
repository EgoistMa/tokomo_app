package org.tokomoapp.tokomoappbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tokomoapp.tokomoappbackend.model.ApiResponse;
import org.tokomoapp.tokomoappbackend.model.Game;
import org.tokomoapp.tokomoappbackend.model.User;
import org.tokomoapp.tokomoappbackend.service.GameService;
import org.tokomoapp.tokomoappbackend.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {
    
    @Autowired
    private GameService gameService;
    
    @Autowired
    private UserService userService;
    
    @Value("${game.cost}")
    private Integer gameCost;
    
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> searchGames(@RequestParam String keyword) {
        try {
            List<Game> games = gameService.searchGames(keyword);
            return ResponseEntity.ok(new ApiResponse("ok", "Games searched successfully", games));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error searching games: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getGame(
            @PathVariable String id,
            @AuthenticationPrincipal Long userId) {
        try {
            User user = userService.getUserById(userId);
            Game game = gameService.getGameById(id)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + id));

            if (user.isVIP()) {
                // VIP用户直接获取游戏信息
                Map<String, Object> response = new HashMap<>();
                response.put("game", game);
                response.put("remainingPoints", user.getPoints());
                return ResponseEntity.ok(new ApiResponse("ok", "Game fetched successfully", response));
            } else {
                // 非VIP用户需要扣除积分
                try {
                    user = userService.deductPoints(userId, gameCost);
                    Map<String, Object> response = new HashMap<>();
                    response.put("game", game);
                    response.put("remainingPoints", user.getPoints());
                    return ResponseEntity.ok(new ApiResponse("ok", "Game fetched successfully", response));
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse("error", "Insufficient points. Required: " + gameCost));
                }
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error fetching game: " + e.getMessage()));
        }
    }

    @PostMapping("/load")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> loadGames() {
        try {
            List<Game> games = gameService.getGames();
            gameService.saveGames(games);
            return ResponseEntity.ok(new ApiResponse("ok", "Games loaded successfully", games));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error loading games: " + e.getMessage()));
        }
    }
}