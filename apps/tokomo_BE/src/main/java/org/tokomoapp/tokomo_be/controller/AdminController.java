package org.tokomoapp.tokomo_be.controller;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tokomoapp.tokomo_be.model.ApiResponse;
import org.tokomoapp.tokomo_be.model.Game;
import org.tokomoapp.tokomo_be.model.PaymentCode;
import org.tokomoapp.tokomo_be.model.User;
import org.tokomoapp.tokomo_be.model.VipCode;
import org.tokomoapp.tokomo_be.dto.UserUpdateDTO;
import org.tokomoapp.tokomo_be.service.GameService;
import org.tokomoapp.tokomo_be.service.PaymentService;
import org.tokomoapp.tokomo_be.service.UserService;
import org.tokomoapp.tokomo_be.service.VipService;
import org.tokomoapp.tokomo_be.util.ExcelGameReader;
import org.tokomoapp.tokomo_be.repository.UserGameRepository;
import org.tokomoapp.tokomo_be.util.ExcelVipReader;
import org.tokomoapp.tokomo_be.util.ExcelPaymentReader;


@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private final UserService userService;
    @Autowired
    private final GameService gameService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private VipService vipService;
    @Autowired
    private UserGameRepository userGameRepository;

    @Autowired
    public AdminController(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @PostMapping("/vip/genVip")
    public ResponseEntity<ApiResponse> generateVipCodes(@RequestBody Map<String, Object> request) {
        try {
            int amount = request.get("amount") != null ? 
                Integer.parseInt(request.get("amount").toString()) : 1;
            int validDays = request.get("validDays") != null ?
                Integer.parseInt(request.get("validDays").toString()) : 30;
                
            List<VipCode> codes = vipService.generateVipCodes(amount, validDays);
            
            return ResponseEntity.ok(new ApiResponse("ok", "VIP codes generated", 
                Map.of("codes", codes)));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/payment/genPay")
    public ResponseEntity<ApiResponse> generatePaymentCode(@RequestBody Map<String, Integer> request) {
        try {
            Integer points = request.get("points");
            Integer amount = request.get("amount");
            
            if (points == null || points <= 0) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Valid points amount is required"));
            }
            
            if (amount == null || amount <= 0) {
                amount = 1;
            }
            
            List<PaymentCode> codes = paymentService.generatePaymentCodes(amount, points);
            
            return ResponseEntity.ok(new ApiResponse("ok", "Payment codes generated successfully", 
                Map.of("codes", codes)));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error generating payment codes: " + e.getMessage()));
        }
    }

    @GetMapping("/game/load")
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



    
    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(new ApiResponse("ok", "Users fetched successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error fetching users: " + e.getMessage()));
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(new ApiResponse("ok", "User fetched successfully", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error fetching user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO updates) {
        try {
            User updatedUser = userService.updateUser(id, updates);
            return ResponseEntity.ok(new ApiResponse("ok", "User updated successfully", updatedUser.sanitize()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("error", e.getMessage()));
        }
    }


    @GetMapping("/games")
    public ResponseEntity<ApiResponse> getAllGames() {
        try {
            List<Game> games = gameService.getAllGames();
            return ResponseEntity.ok(new ApiResponse("ok", "Games fetched successfully", games));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error fetching games: " + e.getMessage()));
        }
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<ApiResponse> getGameById(@PathVariable Long gameId) {
        try {
            Game game = gameService.getGameById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));
            return ResponseEntity.ok(new ApiResponse("ok", "Game fetched successfully", game));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error fetching game: " + e.getMessage()));
        }
    }

    @PutMapping("/games/{gameId}")
    public ResponseEntity<ApiResponse> updateGame(
            @PathVariable Long gameId,
            @RequestBody Game updates) {
        try {
            Game game = gameService.updateGame(gameId, updates);
            return ResponseEntity.ok(new ApiResponse("ok", "Game updated successfully", game));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error updating game: " + e.getMessage()));
        }
    }

    @DeleteMapping("/games/{gameId}")
    public ResponseEntity<ApiResponse> deleteGame(@PathVariable Long gameId) {
        try {
            gameService.deleteGame(gameId);
            return ResponseEntity.ok(new ApiResponse("ok", "Game deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error deleting game: " + e.getMessage()));
        }
    }

    @GetMapping("/vip/codes")
    public ResponseEntity<ApiResponse> getAllVipCodes() {
        try {
            List<VipCode> codes = vipService.getAllVipCodes();
            return ResponseEntity.ok(new ApiResponse("ok", "VIP codes fetched successfully", codes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error fetching VIP codes: " + e.getMessage()));
        }
    }

    @PutMapping("/vip/codes/{id}")
    public ResponseEntity<ApiResponse> updateVipCode(
            @PathVariable Long id,
            @RequestBody VipCode updates) {
        try {
            VipCode updatedCode = vipService.updateVipCode(id, updates);
            return ResponseEntity.ok(new ApiResponse("ok", "VIP code updated successfully", updatedCode));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error updating VIP code: " + e.getMessage()));
        }
    }

    @DeleteMapping("/vip/codes/{id}")
    public ResponseEntity<ApiResponse> deleteVipCode(@PathVariable Long id) {
        try {
            vipService.deleteVipCode(id);
            return ResponseEntity.ok(new ApiResponse("ok", "VIP code deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error deleting VIP code: " + e.getMessage()));
        }
    }

    @GetMapping("/payment/codes")
    public ResponseEntity<ApiResponse> getAllPaymentCodes() {
        try {
            List<PaymentCode> codes = paymentService.getAllPaymentCodes();
            return ResponseEntity.ok(new ApiResponse("ok", "Payment codes fetched successfully", codes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error fetching payment codes: " + e.getMessage()));
        }
    }

    @PutMapping("/payment/codes/{id}")
    public ResponseEntity<ApiResponse> updatePaymentCode(
            @PathVariable Long id,
            @RequestBody PaymentCode updates) {
        try {
            PaymentCode updatedCode = paymentService.updatePaymentCode(id, updates);
            return ResponseEntity.ok(new ApiResponse("ok", "Payment code updated successfully", updatedCode));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error updating payment code: " + e.getMessage()));
        }
    }

    @DeleteMapping("/payment/codes/{id}")
    public ResponseEntity<ApiResponse> deletePaymentCode(@PathVariable Long id) {
        try {
            paymentService.deletePaymentCode(id);
            return ResponseEntity.ok(new ApiResponse("ok", "Payment code deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error deleting payment code: " + e.getMessage()));
        }
    }

    @PostMapping("/games/upload")
    public ResponseEntity<ApiResponse> uploadGames(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "merge") String mode) {
        try {
            String fileName = file.getOriginalFilename();
            String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + fileName;
            file.transferTo(new File(tempFilePath));

            List<Game> newGames = ExcelGameReader.readGamesFromExcel(tempFilePath);

            // 记录重复的游戏名称
            Map<String, List<Game>> duplicateGames = new HashMap<>();
            Map<String, Game> gameMap = new HashMap<>();
            for (Game game : newGames) {
                if (gameMap.containsKey(game.getGameName())) {
                    duplicateGames.computeIfAbsent(game.getGameName(), k -> new ArrayList<>())
                        .add(game);
                }
                gameMap.put(game.getGameName(), game);
            }
            newGames = gameMap.values().stream().collect(Collectors.toList());

            List<Game> skippedGames = new ArrayList<>();
            if ("overwrite".equals(mode)) {
                List<Game> allGames = gameService.getAllGames();
                for (Game game : allGames) {
                    if (userGameRepository.existsByGameId(game.getId())) {
                        skippedGames.add(game);
                    }
                }
                gameService.deleteAllGames();
                gameService.saveGames(newGames);
            } else if ("merge".equals(mode)) {
                List<Game> mergedGames = gameService.mergeGames(newGames);
                newGames = mergedGames;
            } else {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Invalid mode. Use 'merge' or 'overwrite'"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("games", newGames);
            if (!duplicateGames.isEmpty()) {
                response.put("duplicateGames", duplicateGames);
            }
            if (!skippedGames.isEmpty()) {
                response.put("skippedGames", skippedGames);
            }

            return ResponseEntity.ok(new ApiResponse("ok", 
                String.format("Games processed successfully. Found %d duplicates, skipped %d games", 
                    duplicateGames.size(), skippedGames.size()), 
                response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error uploading games: " + e.getMessage()));
        }
    }

    @PostMapping("/vip/set")
    public ResponseEntity<ApiResponse> setVipCodes(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + fileName;
            file.transferTo(new File(tempFilePath));

            // 读取Excel文件中的VIP码数据
            List<VipCode> newCodes = ExcelVipReader.readVipCodesFromExcel(tempFilePath);

            // 删除所有现有VIP码并保存新的
            vipService.deleteAllVipCodes();
            List<VipCode> savedCodes = vipService.saveVipCodes(newCodes);

            return ResponseEntity.ok(new ApiResponse("ok", 
                String.format("Successfully imported %d VIP codes", savedCodes.size()), 
                savedCodes));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error uploading VIP codes: " + e.getMessage()));
        }
    }

    @PostMapping("/payment/setPay")
    public ResponseEntity<ApiResponse> setPaymentCodes(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + fileName;
            file.transferTo(new File(tempFilePath));

            // 读取Excel文件中的支付码数据
            List<PaymentCode> newCodes = ExcelPaymentReader.readPaymentCodesFromExcel(tempFilePath);

            // 删除所有现有支付码并保存新的
            paymentService.deleteAllPaymentCodes();
            List<PaymentCode> savedCodes = paymentService.savePaymentCodes(newCodes);

            return ResponseEntity.ok(new ApiResponse("ok", 
                String.format("Successfully imported %d payment codes", savedCodes.size()), 
                savedCodes));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error uploading payment codes: " + e.getMessage()));
        }
    }
}
