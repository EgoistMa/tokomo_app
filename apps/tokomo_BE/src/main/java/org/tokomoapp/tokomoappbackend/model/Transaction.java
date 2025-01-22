package org.tokomoapp.tokomoappbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "transactions")
@ToString
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;
    
    @Column(nullable = false)
    private Integer amount;
    
    @Column(name = "from_user")
    private Long fromUser;
    
    @Column(name = "external_transaction_key", unique = true)
    private String externalTransactionKey;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    public enum PaymentType {
        WECHAT_PAY,
        ALI_PAY
    }
    
    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
    
    public Transaction(PaymentType type, Integer amount, Long fromUser, String externalTransactionKey) {
        this.type = type;
        this.amount = amount;
        this.fromUser = fromUser;
        this.externalTransactionKey = externalTransactionKey;
        this.createdAt = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
    }
} 
