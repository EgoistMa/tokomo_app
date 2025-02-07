package org.tokomoapp.tokomo_be.controller;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tokomoapp.tokomo_be.model.ApiResponse;
import org.tokomoapp.tokomo_be.model.Game;
import org.tokomoapp.tokomo_be.model.User;
import org.tokomoapp.tokomo_be.service.GameService;
import org.tokomoapp.tokomo_be.service.UserGameService;
import org.tokomoapp.tokomo_be.service.UserService;

@RestController
@RequestMapping("/api/games")
@PreAuthorize("isAuthenticated()")
public class GameController {
    

    
    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserGameService userGameService;

    @Value("${game.cost}")
    private Integer gameCost;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchGames(@RequestParam String keyword) {
        try {
            List<Game> games = gameService.searchGames(keyword);
            //sort by game id 
            games.sort(Comparator.comparingLong(Game::getId));
            return ResponseEntity.ok(new ApiResponse("ok", "Games searched successfully", games));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error searching games: " + e.getMessage()));
        }
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<ApiResponse> getGame(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId) {
        try {
            User user = userService.getUserById(userId);
            Game game = gameService.getGameById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with name: " + gameId));

            // 检查用户是否已购买游戏或是VIP
            boolean hasAccess = user.isVIP() || userGameService.existsByUserIdAndGameId(userId, game.getId());

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

    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse> purchaseGame(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Long userId) {
        try {
            Long gameId = Long.parseLong(request.get("gameId"));

            User user = userService.getUserById(userId);
            Game game = gameService.getGameById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with : " + gameId));

            // 检查用户是否已经购买过该游戏
            if (userGameService.existsByUserIdAndGameId(user.getId(), game.getId())) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "You already own this game"));
            }

            // 非VIP用户需要扣除积分
            if (!user.isVIP()) {
                user = userService.deductPoints(user.getId(), gameCost);
            }

            // 保存购买记录
           userGameService.save(user, game);

            Map<String, Object> response = new HashMap<>();
            response.put("game", game);
            response.put("remainingPoints", user.getPoints());
            
            return ResponseEntity.ok(new ApiResponse("ok", "Game purchased successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error purchasing game: " + e.getMessage()));
        }
    }
}
