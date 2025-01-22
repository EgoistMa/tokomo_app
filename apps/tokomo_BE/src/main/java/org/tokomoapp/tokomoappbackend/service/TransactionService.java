package org.tokomoapp.tokomoappbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tokomoapp.tokomoappbackend.model.Transaction;
import org.tokomoapp.tokomoappbackend.model.Transaction.TransactionStatus;
import org.tokomoapp.tokomoappbackend.model.User;
import org.tokomoapp.tokomoappbackend.repository.TransactionRepository;
import org.tokomoapp.tokomoappbackend.repository.UserRepository;
import java.util.List;

@Service
@Transactional
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public Transaction resolveTransaction(String externalTransactionKey, String resolveType) {
        Transaction trans = transactionRepository.findByExternalTransactionKey(externalTransactionKey)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        if (resolveType.equals("success")) {
            trans.setStatus(TransactionStatus.COMPLETED);
            
            // 更新用户积分
            User user = userRepository.findById(trans.getFromUser())
                .orElseThrow(() -> new RuntimeException("User not found"));
            user.setPoints(user.getPoints() + trans.getAmount()*10);
            userRepository.save(user);
            
        } else {
            trans.setStatus(TransactionStatus.FAILED);
        }

        return transactionRepository.save(trans);
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findByUserId(Long userId) {
        return transactionRepository.findByFromUserOrderByCreatedAtDesc(userId);
    }
} 