package org.tokomoapp.tokomo_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tokomoapp.tokomo_be.model.PaymentCode;
import java.util.List;
import java.util.Optional;

public interface PaymentCodeRepository extends JpaRepository<PaymentCode, Long> {
    Optional<PaymentCode> findByCode(String code);
    List<PaymentCode> findByUsedByOrderByUsedAtDesc(Long usedBy);
    Optional<PaymentCode> findByCodeAndUsedFalse(String code);
    boolean existsByCode(String code);
} 