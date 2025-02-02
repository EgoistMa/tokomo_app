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
import org.tokomoapp.tokomoappbackend.model.UserGame;
import org.tokomoapp.tokomoappbackend.repository.UserGameRepository;
import org.tokomoapp.tokomoappbackend.service.GameService;
import org.tokomoapp.tokomoappbackend.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
@PreAuthorize("isAuthenticated()")
public class GameController {
    
    @Autowired
    private GameService gameService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserGameRepository userGameRepository;
    
    @Value("${game.cost}")
    private Integer gameCost;
    
    @GetMapping("/search")

    public ResponseEntity<ApiResponse> searchGames(@RequestParam String keyword) {
        try {
            List<Game> games = gameService.searchGames(keyword);
            return ResponseEntity.ok(new ApiResponse("ok", "Games searched successfully", games));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error searching games: " + e.getMessage()));
        }
    }

    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse> purchaseGame(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Long userId) {
        try {
            String gameId = request.get("gameId");
            if (gameId == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Game ID is required"));
            }

            User user = userService.getUserById(userId);
            Game game = gameService.getGameById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));

            // 检查用户是否已经购买过该游戏
            if (userGameRepository.existsByUserIdAndGameId(userId, gameId)) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "You already own this game"));
            }

            // 非VIP用户需要扣除积分
            if (!user.isVIP()) {
                user = userService.deductPoints(userId, gameCost);
            }

            // 保存购买记录
            UserGame userGame = new UserGame(user, game);
            userGameRepository.save(userGame);

            Map<String, Object> response = new HashMap<>();
            response.put("game", game);
            response.put("remainingPoints", user.getPoints());
            
            return ResponseEntity.ok(new ApiResponse("ok", "Game purchased successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error purchasing game: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getGame(
            @PathVariable String id,
            @AuthenticationPrincipal Long userId) {
        try {
            User user = userService.getUserById(userId);
            Game game = gameService.getGameById(id)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + id));

            // 检查用户是否已购买游戏或是VIP
            boolean hasAccess = user.isVIP() || userGameRepository.existsByUserIdAndGameId(userId, id);

            if (hasAccess) {
                Map<String, Object> response = new HashMap<>();
                response.put("game", game);
                response.put("remainingPoints", user.getPoints());
                return ResponseEntity.ok(new ApiResponse("ok", "Game fetched successfully", response));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("error", "Please purchase the game first. Required points: " + gameCost));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error fetching game: " + e.getMessage()));
        }
    }

    // 添加新接口：获取用户购买的所有游戏
    @GetMapping("/purchased")
    public ResponseEntity<ApiResponse> getPurchasedGames(@AuthenticationPrincipal Long userId) {
        try {
            List<UserGame> userGames = userGameRepository.findByUserId(userId);
            List<Game> games = userGames.stream()
                .map(UserGame::getGame)
                .toList();
            
            return ResponseEntity.ok(new ApiResponse("ok", "Purchased games fetched successfully", games));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error fetching purchased games: " + e.getMessage()));
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