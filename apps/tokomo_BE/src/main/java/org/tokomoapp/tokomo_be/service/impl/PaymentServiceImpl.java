package org.tokomoapp.tokomo_be.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.tokomoapp.tokomo_be.model.PaymentCode;
import org.tokomoapp.tokomo_be.repository.PaymentCodeRepository;
import org.tokomoapp.tokomo_be.service.PaymentService;
import org.tokomoapp.tokomo_be.util.CodeUtil;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentCodeRepository paymentCodeRepository;

    @Transactional
    @Override
    public List<PaymentCode> generatePaymentCodes(int amount, int values) {
        List<PaymentCode> codes = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            String code;
            do {
                code = CodeUtil.generateRandomCode();
            } while (paymentCodeRepository.existsByCode(code));
            
            PaymentCode paymentCode = new PaymentCode();
            paymentCode.setCode(code);
            paymentCode.setPoints(values);
            paymentCode.setUsed(false);
            codes.add(paymentCodeRepository.save(paymentCode));
        }
        return codes;
    }

    @Override
    public List<PaymentCode> getPaymentHistory(Long userId) {
        return paymentCodeRepository.findByUsedByOrderByUsedAtDesc(userId);
    }

    @Override
    public List<PaymentCode> getAllPaymentCodes() {
        return paymentCodeRepository.findAll();
    }

    @Override
    @Transactional
    public PaymentCode updatePaymentCode(Long id, PaymentCode updates) {
        PaymentCode paymentCode = paymentCodeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment code not found"));
        
        if (updates.getPoints() != null) paymentCode.setPoints(updates.getPoints());
        if (updates.getUsed() != null) paymentCode.setUsed(updates.getUsed());
        if (updates.getUsedBy() != null) paymentCode.setUsedBy(updates.getUsedBy());
        if (updates.getUsedAt() != null) paymentCode.setUsedAt(updates.getUsedAt());
        
        return paymentCodeRepository.save(paymentCode);
    }

    @Override
    @Transactional
    public void deletePaymentCode(Long id) {
        PaymentCode paymentCode = paymentCodeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment code not found"));
        paymentCodeRepository.delete(paymentCode);
    }

    @Override
    @Transactional
    public void deleteAllPaymentCodes() {
        paymentCodeRepository.deleteAll();
    }

    @Override
    @Transactional
    public List<PaymentCode> savePaymentCodes(List<PaymentCode> codes) {
        return paymentCodeRepository.saveAll(codes);
    }
}
