package org.tokomoapp.tokomoappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tokomoapp.tokomoappbackend.model.SecurityQuestion;

public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, Long> {
} 