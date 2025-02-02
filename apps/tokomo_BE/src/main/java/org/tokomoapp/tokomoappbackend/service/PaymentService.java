package org.tokomoapp.tokomoappbackend.service;

import org.tokomoapp.tokomoappbackend.model.PaymentCode;

import java.util.List;

public interface PaymentService {

    PaymentCode generateCode(Integer points);

    PaymentCode redeemCode(Long userId, String code);

    List<PaymentCode> getPaymentHistory(Long userId);
} 