package org.tokomoapp.tokomoappbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tokomoapp.tokomoappbackend.model.ApiResponse;
import org.tokomoapp.tokomoappbackend.model.Transaction;
import org.tokomoapp.tokomoappbackend.service.UserService;
import org.tokomoapp.tokomoappbackend.exception.UserAlreadyExistsException;
import org.tokomoapp.tokomoappbackend.util.JwtUtil;
import org.tokomoapp.tokomoappbackend.model.User;
import java.util.List;
import java.util.Map;
import org.tokomoapp.tokomoappbackend.service.TransactionService;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;
import org.tokomoapp.tokomoappbackend.exception.InvalidVipCodeException;
import org.tokomoapp.tokomoappbackend.model.SecurityQuestion;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TransactionService transactionService;
    
    @Value("${game.cost}")
    private Integer GameCost;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String question = request.get("question");
            String answer = request.get("answer");
            String vipCode = request.get("vipCode");  // 可选的VIP码
            
            if (username == null || password == null || question == null || answer == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Username, password, security question and answer are required"));
            }
            
            User user = userService.registerUser(username, password, question, answer, vipCode);
            
            try {
                String token = jwtUtil.generateToken(user);
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("ok", "Registration successful", data));
            } catch (Exception e) {
                System.out.println("Error generating token: " + e.getMessage());
                e.printStackTrace();
                
                // 即使token生成失败，注册仍然是成功的
                Map<String, Object> data = new HashMap<>();
                data.put("userId", user.getId());
                data.put("vipExpireDate", user.getVipExpireDate());
                
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("ok", "Registration successful but token generation failed", data));
            }
                    
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Registration failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            if (username == null || password == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Username and password are required"));
            }
            
            try {
                User user = userService.authenticateUser(username, password);
                String token = jwtUtil.generateToken(user);
                
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                
                return ResponseEntity.ok(new ApiResponse("ok", "Login successful", data));
            } catch (RuntimeException e) {
                e.printStackTrace(); // 打印详细错误信息
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("error", e.getMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/redeem-vip")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> redeemVipCode(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            
            String code = request.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Redeem code is required"));
            }
            
            User user = userService.redeemVipCode(userId, code);
            
            return ResponseEntity.ok(new ApiResponse("ok", "VIP activated successfully", 
                Map.of("expireDate", user.getVipExpireDate())));
                
        } catch (InvalidVipCodeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "System error: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getUserProfile(
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("error", "Invalid token"));
            }
            
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("error", "Invalid token"));
            }
            
            User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // 获取用户的所有交易记录
            List<Transaction> transactions = transactionService.findByUserId(userId);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("hashedPassword", ""); // 密码留空
            userInfo.put("points", user.getPoints());
            userInfo.put("vipExpireDate", user.getVipExpireDate());
            userInfo.put("isAdmin", user.getIsAdmin());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("lastLoginAt", user.getLastLoginAt());
            userInfo.put("isActive", user.getIsActive());
            userInfo.put("transactions", transactions);
            
            return ResponseEntity.ok(new ApiResponse("ok", "User profile retrieved", userInfo));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/password/security-question")
    public ResponseEntity<ApiResponse> getSecurityQuestion(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            if (username == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Username is required"));
            }

            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            SecurityQuestion securityQuestion = userService.getSecurityQuestion(user.getSecurityQuestionId());
            
            return ResponseEntity.ok(new ApiResponse("ok", "Security question fetched", 
                Map.of("question", securityQuestion.getQuestion())));
                
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error fetching security question: " + e.getMessage()));
        }
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String answer = request.get("answer");
            String newPassword = request.get("newPassword");
            
            if (username == null || answer == null || newPassword == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Username, answer and new password are required"));
            }

            userService.resetPassword(username, answer, newPassword);
            
            return ResponseEntity.ok(new ApiResponse("ok", "Password reset successfully"));
                
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error resetting password: " + e.getMessage()));
        }
    }
} 
