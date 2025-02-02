package org.tokomoapp.tokomoappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tokomoapp.tokomoappbackend.model.PaymentCode;
import java.util.List;
import java.util.Optional;

public interface PaymentCodeRepository extends JpaRepository<PaymentCode, Long> {
    Optional<PaymentCode> findByCode(String code);
    List<PaymentCode> findByUsedByOrderByUsedAtDesc(Long usedBy);
} 