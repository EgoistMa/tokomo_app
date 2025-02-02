package org.tokomoapp.tokomoappbackend.service;

import org.tokomoapp.tokomoappbackend.model.User;
import org.tokomoapp.tokomoappbackend.model.VipCode;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(String username, String password, String securityQuestion, String securityAnswer);
    User loginUser(String username, String password);
    User getUserById(Long id);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    User updateUser(Long id, User updates);
    void updateUserStatus(Long id, boolean isActive);
    String resetPassword(String username, String securityAnswer, String newPassword);
    String getSecurityQuestion(String username);
    List<VipCode> generateVipCodes(int amount, int validDays);
    User addPoints(Long userId, Integer points);
    User deductPoints(Long userId, Integer points);
    User authenticateUser(String username, String password);
    User redeemVipCode(Long userId, String code);
    Optional<User> findById(Long userId);
} 