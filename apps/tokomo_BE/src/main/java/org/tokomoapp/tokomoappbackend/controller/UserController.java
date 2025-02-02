package org.tokomoapp.tokomoappbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tokomoapp.tokomoappbackend.model.ApiResponse;
import org.tokomoapp.tokomoappbackend.model.PaymentCode;
import org.tokomoapp.tokomoappbackend.service.PaymentService;
import org.tokomoapp.tokomoappbackend.service.UserService;
import org.tokomoapp.tokomoappbackend.exception.UserAlreadyExistsException;
import org.tokomoapp.tokomoappbackend.util.JwtUtil;
import org.tokomoapp.tokomoappbackend.model.User;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
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
    private PaymentService paymentService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${game.cost}")
    private Integer GameCost;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String securityQuestion = request.get("securityQuestion");
            String securityAnswer = request.get("securityAnswer");

            User user = userService.registerUser(username, password, securityQuestion, securityAnswer);
            return ResponseEntity.ok(new ApiResponse("ok", "Registration successful", user.sanitize()));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Registration failed"));
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

    @PostMapping("/redeem-payment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> redeemPaymentCode(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            
            String code = request.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Payment code is required"));
            }
            
            User user = userService.getUserById(userId);
            PaymentCode paymentCode = paymentService.redeemCode(userId, code);
            
            return ResponseEntity.ok(new ApiResponse("ok", "Payment successful", 
                Map.of("points", paymentCode.getPoints(),
                       "totalPoints", user.getPoints())));
                
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "System error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
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
            
            // TODO: 获取用户的所有交易记录
            //List<Transaction> transactions = transactionService.findByUserId(userId);
            List<Integer> transactions = new ArrayList<>();
            
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
            userInfo.put("transactions", transactions); // TODO: 获取用户的所有交易记录
            
            return ResponseEntity.ok(new ApiResponse("ok", "User profile retrieved", userInfo));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", e.getMessage()));
        }
    }

    @GetMapping("/password/security-question")
    public ResponseEntity<ApiResponse> getSecurityQuestion(@RequestParam String username) {
        try {
            String question = userService.getSecurityQuestion(username);
            return ResponseEntity.ok(new ApiResponse("ok", "Success", question));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String securityAnswer = request.get("securityAnswer");
            String newPassword = request.get("newPassword");

            String result = userService.resetPassword(username, securityAnswer, newPassword);
            return ResponseEntity.ok(new ApiResponse("ok", result));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("error", e.getMessage()));
        }
    }

    @GetMapping("/payment-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getPaymentHistory(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            
            List<PaymentCode> paymentHistory = paymentService.getPaymentHistory(userId);
            
            return ResponseEntity.ok(new ApiResponse("ok", "Payment history retrieved", paymentHistory));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error retrieving payment history: " + e.getMessage()));
        }
    }
} 
