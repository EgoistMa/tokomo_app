package org.tokomoapp.tokomo_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tokomoapp.tokomo_be.model.VipCode;
import java.util.Optional;

public interface VipCodeRepository extends JpaRepository<VipCode, Long> {
    Optional<VipCode> findByCodeAndUsedFalse(String code);
    boolean existsByCode(String code);
} 