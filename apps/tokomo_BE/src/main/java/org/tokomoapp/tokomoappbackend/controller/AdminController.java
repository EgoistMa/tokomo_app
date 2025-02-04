package org.tokomoapp.tokomoappbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.tokomoapp.tokomoappbackend.model.ApiResponse;
import org.tokomoapp.tokomoappbackend.model.VipCode;
import org.tokomoapp.tokomoappbackend.service.UserService;
import org.tokomoapp.tokomoappbackend.model.User;
import org.tokomoapp.tokomoappbackend.model.Game;
import org.tokomoapp.tokomoappbackend.model.PaymentCode;
import org.tokomoapp.tokomoappbackend.service.GameService;
import org.tokomoapp.tokomoappbackend.service.PaymentService;
import org.tokomoapp.tokomoappbackend.util.ExcelGameReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private GameService gameService;

    @Autowired
    private PaymentService paymentService;


    @PostMapping("/vip/genVip")
    public ResponseEntity<ApiResponse> generateVipCodes(@RequestBody Map<String, Object> request) {
        try {
            int amount = request.get("amount") != null ? 
                Integer.parseInt(request.get("amount").toString()) : 1;
            int validDays = request.get("validDays") != null ?
                Integer.parseInt(request.get("validDays").toString()) : 30;
                
            List<VipCode> codes = userService.generateVipCodes(amount, validDays);
            
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
            
            List<PaymentCode> codes = new ArrayList<>();
            for (int i = 0; i < amount; i++) {
                codes.add(paymentService.generateCode(points));
            }
            
            return ResponseEntity.ok(new ApiResponse("ok", "Payment codes generated successfully", 
                Map.of("codes", codes)));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error generating payment codes: " + e.getMessage()));
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
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id, @RequestBody User updates) {
        try {
            User updatedUser = userService.updateUser(id, updates);
            return ResponseEntity.ok(new ApiResponse("ok", "User updated successfully", updatedUser.sanitize()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        try {
            boolean isActive = request.get("isActive");
            userService.updateUserStatus(id, isActive);
            return ResponseEntity.ok(new ApiResponse("ok", "User status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
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
    public ResponseEntity<ApiResponse> getGameById(@PathVariable String gameId) {
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
            @PathVariable String gameId,
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

    @PostMapping("/games/upload")
    public ResponseEntity<ApiResponse> uploadGames(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mode") String mode) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Please select a file to upload"));
            }

            // 检查文件扩展名
            String filename = file.getOriginalFilename();
            if (!filename.endsWith(".xlsx") && !filename.endsWith(".xls")) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Only Excel files (.xlsx or .xls) are allowed"));
            }

            // 保存上传的文件到临时目录
            Path tempFile = Files.createTempFile("games", filename);
            file.transferTo(tempFile.toFile());

            // 读取Excel文件
            List<Game> newGames = ExcelGameReader.readGamesFromExcel(tempFile.toString());

            // 根据模式处理数据
            if ("overwrite".equalsIgnoreCase(mode)) {
                gameService.overwriteGames(newGames);
            } else if ("merge".equalsIgnoreCase(mode)) {
                gameService.mergeGames(newGames);
            } else {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Invalid mode. Use 'merge' or 'overwrite'"));
            }

            // 删除临时文件
            Files.delete(tempFile);

            return ResponseEntity.ok(new ApiResponse("ok", 
                String.format("Games %s successfully", mode.toLowerCase()), 
                Map.of("count", newGames.size())));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error uploading games: " + e.getMessage()));
        }
    }
} 