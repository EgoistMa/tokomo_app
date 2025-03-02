package org.tokomoapp.tokomo_be.service;

import org.tokomoapp.tokomo_be.dto.UserUpdateDTO;
import org.tokomoapp.tokomo_be.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    User getUserById(Long userId);

    void saveUser(User user);

    User registerUser(String username, String password, String securityQuestion, String securityAnswer);

    User authenticateUser(String username, String password);

    User redeemVipCode(Long userId, String code);
    User redeemPaymentCode(Long userId, String code);

    Optional<User> findById(Long userId);

    String getSecurityQuestion(String username);

    String resetPassword(String username, String securityAnswer, String newPassword);

    User deductPoints(Long userId, Integer points);

    List<User> getAllUsers();

    User updateUser(Long id, UserUpdateDTO updates);

    User deletUser(Long id);

} 