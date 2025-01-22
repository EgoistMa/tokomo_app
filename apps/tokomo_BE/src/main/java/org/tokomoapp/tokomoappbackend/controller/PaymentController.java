package org.tokomoapp.tokomoappbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tokomoapp.tokomoappbackend.model.ApiResponse;
import org.tokomoapp.tokomoappbackend.model.Transaction;
import org.tokomoapp.tokomoappbackend.model.Transaction.PaymentType;
import org.tokomoapp.tokomoappbackend.service.TransactionService;
import org.tokomoapp.tokomoappbackend.util.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> createPayment(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Integer> request) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            
            Integer amount = request.get("amount");
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Invalid deposit amount"));
            }
            
            String externalTransactionKey = "TX_" + System.currentTimeMillis() + "_" + userId;
            
            Transaction transaction = new Transaction(PaymentType.WECHAT_PAY, amount, userId, externalTransactionKey);
            transactionService.saveTransaction(transaction);
            
            return ResponseEntity.ok(new ApiResponse("ok", "Payment Transaction created", 
                Map.of("transaction", transaction)));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", "System error"));
        }
    }

    @PostMapping("/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> resolvePayment(@RequestBody Map<String, String> request) {
        try {
            String externalTransactionKey = request.get("externalTransactionKey");
            String resolveType = request.get("resolveType");
            
            if (externalTransactionKey == null || resolveType == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "externalTransactionKey and resolve Type are required"));
            }
            
            Transaction transaction = transactionService.resolveTransaction(externalTransactionKey, resolveType);
            return ResponseEntity.ok(new ApiResponse("ok", "Transaction resolved", transaction));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("error", e.getMessage()));
        }
    }
} 