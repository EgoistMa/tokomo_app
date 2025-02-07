package org.tokomoapp.tokomo_be.service;

import java.util.List;

import org.tokomoapp.tokomo_be.model.PaymentCode;

public interface PaymentService {
    List<PaymentCode> generatePaymentCodes(int amount, int values);

    List<PaymentCode> getPaymentHistory(Long userId);

    List<PaymentCode> getAllPaymentCodes();

    PaymentCode updatePaymentCode(Long id, PaymentCode updates);

    void deletePaymentCode(Long id);

    void deleteAllPaymentCodes();

    List<PaymentCode> savePaymentCodes(List<PaymentCode> codes);
}
