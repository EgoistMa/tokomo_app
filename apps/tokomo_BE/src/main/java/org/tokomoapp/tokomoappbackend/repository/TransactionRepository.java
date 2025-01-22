package org.tokomoapp.tokomoappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tokomoapp.tokomoappbackend.model.Transaction;
import java.util.Optional;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByExternalTransactionKey(String externalTransactionKey);
    List<Transaction> findByFromUserOrderByCreatedAtDesc(Long userId);
} 