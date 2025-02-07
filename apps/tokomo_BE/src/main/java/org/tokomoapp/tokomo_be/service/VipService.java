package org.tokomoapp.tokomo_be.service;

import java.util.List;

import org.tokomoapp.tokomo_be.model.VipCode;

public interface VipService {
    List<VipCode> generateVipCodes(int amount, int validDays);
    List<VipCode> getAllVipCodes();
    VipCode updateVipCode(Long id, VipCode updates);
    void deleteVipCode(Long id);
    void deleteAllVipCodes();
    List<VipCode> saveVipCodes(List<VipCode> codes);
}
