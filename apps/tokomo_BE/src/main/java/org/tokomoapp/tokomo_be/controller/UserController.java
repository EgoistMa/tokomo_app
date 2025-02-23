package org.tokomoapp.tokomo_be.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tokomoapp.tokomo_be.model.ApiResponse;
import org.tokomoapp.tokomo_be.model.PaymentCode;
import org.tokomoapp.tokomo_be.model.User;
import org.tokomoapp.tokomo_be.model.UserGame;
import org.tokomoapp.tokomo_be.service.PaymentService;
import org.tokomoapp.tokomo_be.service.UserGameService;
import org.tokomoapp.tokomo_be.service.UserService;
import org.tokomoapp.tokomo_be.util.JwtUtil;
import org.tokomoapp.tokomo_be.dto.UserGameDTO;
import org.tokomoapp.tokomo_be.exception.InvalidVipCodeException;
import org.tokomoapp.tokomo_be.exception.UserAlreadyExistsException;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserGameService userGameService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String securityQuestion = request.get("securityQuestion");
            String securityAnswer = request.get("securityAnswer");

            User user = userService.registerUser(username, password, securityQuestion, securityAnswer);
            
            // add vip code if exists
            String VipCode = request.get("VipCode");
            if (VipCode != null) {
                userService.redeemVipCode(user.getId(), VipCode);
            }

            String token =  jwtUtil.generateToken(user);
            Map<String, Object> data = new HashMap<>();
            data.put("user",user.sanitize());
            data.put("token", token);

            return ResponseEntity.ok(new ApiResponse("ok", "Registration successful", data));
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
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("error", e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getUserProfile(
            @AuthenticationPrincipal Long userId
            ) {
        try {
            User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
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
            
            return ResponseEntity.ok(new ApiResponse("ok", "User profile retrieved", userInfo));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/redeem-vip")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> redeemVipCode(
        @AuthenticationPrincipal Long userId,
        @RequestBody Map<String, String> request
        ) {

        try {
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
            @AuthenticationPrincipal Long userId,
            @RequestBody Map<String, String> request) {
        try {

            String code = request.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Payment code is required"));
            }
            
            User user = userService.redeemPaymentCode(userId,code);
            
            return ResponseEntity.ok(new ApiResponse("ok", "Payment successful", 
                Map.of("totalPoints", user.getPoints())));
                
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "System error: " + e.getMessage()));
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
            String securityAnswer = request.get("answer");
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
            @AuthenticationPrincipal Long userId) {
        try {
            
            List<PaymentCode> paymentHistory = paymentService.getPaymentHistory(userId);
            
            return ResponseEntity.ok(new ApiResponse("ok", "Payment history retrieved", paymentHistory));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error retrieving payment history: " + e.getMessage()));
        }
    }

    @GetMapping("/purchase-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> getPurchaseHistory(
            @AuthenticationPrincipal Long userId) {
        try {
            List<UserGame> purchaseHistory = userGameService.getPurchaseHistory(userId);

            List<UserGameDTO> purchaseHistoryDTO = purchaseHistory.stream()
                .map(userGame -> new UserGameDTO(
                    userGame.getId(),
                    userGame.getUser().getId(),
                    userGame.getGame(),
                    userGame.getPurchaseDate()
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse("ok", "Purchase history retrieved", purchaseHistoryDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "Error retrieving purchase history: " + e.getMessage()));
        }
    }
}
