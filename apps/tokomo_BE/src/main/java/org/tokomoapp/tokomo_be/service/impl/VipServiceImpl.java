package org.tokomoapp.tokomo_be.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.tokomoapp.tokomo_be.model.VipCode;
import org.tokomoapp.tokomo_be.repository.VipCodeRepository;
import org.tokomoapp.tokomo_be.service.VipService;
import org.tokomoapp.tokomo_be.util.CodeUtil;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
public class VipServiceImpl implements VipService {
    @Autowired
    private VipCodeRepository vipCodeRepository;

    @Transactional
    public List<VipCode> generateVipCodes(int amount, int validDays) {
        List<VipCode> codes = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            String code;
            do {
                code = CodeUtil.generateRandomCode();
            } while (vipCodeRepository.existsByCode(code));
            
            VipCode vipCode = new VipCode();
            vipCode.setCode(code);
            vipCode.setValidDays(validDays);
            vipCode.setUsed(false);
            codes.add(vipCodeRepository.save(vipCode));
        }
        return codes;
    }

    @Override
    public List<VipCode> getAllVipCodes() {
        return vipCodeRepository.findAll();
    }

    @Override
    @Transactional
    public VipCode updateVipCode(Long id, VipCode updates) {
        VipCode vipCode = vipCodeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("VIP code not found"));

        if (updates.getValidDays() != null) vipCode.setValidDays(updates.getValidDays());
        if (updates.isUsed() != vipCode.isUsed()) vipCode.setUsed(updates.isUsed());
        if (updates.getUsedBy() != null) vipCode.setUsedBy(updates.getUsedBy());
        if (updates.getUsedAt() != null) vipCode.setUsedAt(updates.getUsedAt());
        
        return vipCodeRepository.save(vipCode);
    }

    @Override
    @Transactional
    public void deleteVipCode(Long id) {
        VipCode vipCode = vipCodeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("VIP code not found"));
        vipCodeRepository.delete(vipCode);
    }

    @Override
    @Transactional
    public void deleteAllVipCodes() {
        vipCodeRepository.deleteAll();
    }

    @Override
    @Transactional
    public List<VipCode> saveVipCodes(List<VipCode> codes) {
        return vipCodeRepository.saveAll(codes);
    }
}
