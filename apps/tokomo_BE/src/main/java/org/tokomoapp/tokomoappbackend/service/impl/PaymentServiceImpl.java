package org.tokomoapp.tokomoappbackend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tokomoapp.tokomoappbackend.model.PaymentCode;
import org.tokomoapp.tokomoappbackend.repository.PaymentCodeRepository;
import org.tokomoapp.tokomoappbackend.service.PaymentService;
import org.tokomoapp.tokomoappbackend.service.UserService;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    
    @Autowired
    private PaymentCodeRepository paymentCodeRepository;
    
    @Autowired
    private UserService userService;

    @Override
    public PaymentCode generateCode(Integer points) {
        PaymentCode code = new PaymentCode();
        code.setCode(UUID.randomUUID().toString().substring(0, 8));
        code.setPoints(points);
        return paymentCodeRepository.save(code);
    }

    @Override
    public PaymentCode redeemCode(Long userId, String code) {
        PaymentCode paymentCode = paymentCodeRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Invalid payment code"));
            
        if (paymentCode.getUsed()) {
            throw new RuntimeException("Payment code already used");
        }
        
        paymentCode.setUsed(true);
        paymentCode.setUsedBy(userId);
        paymentCode.setUsedAt(LocalDateTime.now());
        
        userService.addPoints(userId, paymentCode.getPoints());
        
        return paymentCodeRepository.save(paymentCode);
    }

    @Override
    public List<PaymentCode> getPaymentHistory(Long userId) {
        return paymentCodeRepository.findByUsedByOrderByUsedAtDesc(userId);
    }
} 