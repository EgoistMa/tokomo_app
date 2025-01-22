package org.tokomoapp.tokomoappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tokomoapp.tokomoappbackend.model.VipCode;
import java.util.Optional;

@Repository
public interface VipCodeRepository extends JpaRepository<VipCode, Long> {
    Optional<VipCode> findByCodeAndUsedFalse(String code);
    boolean existsByCode(String code);
} 