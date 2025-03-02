package org.tokomoapp.tokomo_be.service.impl;

import org.tokomoapp.tokomo_be.exception.InsufficientPointsException;
import org.tokomoapp.tokomo_be.exception.InvalidVipCodeException;
import org.tokomoapp.tokomo_be.exception.UserAlreadyExistsException;
import org.tokomoapp.tokomo_be.model.PaymentCode;
import org.tokomoapp.tokomo_be.model.User;
import org.tokomoapp.tokomo_be.model.VipCode;
import org.tokomoapp.tokomo_be.service.UserService;

import jakarta.transaction.Transactional;

import org.tokomoapp.tokomo_be.repository.PaymentCodeRepository;
import org.tokomoapp.tokomo_be.repository.UserRepository;
import org.tokomoapp.tokomo_be.repository.VipCodeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.tokomoapp.tokomo_be.dto.UserUpdateDTO;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private final UserRepository userRepository;
    private final PaymentCodeRepository paymentCodeRepository;
    private final VipCodeRepository vipCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                         PaymentCodeRepository paymentCodeRepository,
                         VipCodeRepository vipCodeRepository,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.paymentCodeRepository = paymentCodeRepository;
        this.vipCodeRepository = vipCodeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"))
                .sanitize();
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User registerUser(String username, String password, String securityQuestion, String securityAnswer) {

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        User user = new User(
            username,
            passwordEncoder.encode(password),
            securityQuestion,
            securityAnswer
        );
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User authenticateUser(String username, String password) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户名不存在"));

            if (!passwordEncoder.matches(password, user.getHashedPassword())) {
                throw new RuntimeException("密码错误");
            }

            return user;
        } catch (RuntimeException e) {
            // 直接抛出 RuntimeException，保留原始错误信息
            throw e;
        } catch (Exception e) {
            // 其他未预期的错误才包装为 internal error
            throw new RuntimeException("系统内部错误: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public User redeemVipCode(Long userId, String code) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        VipCode vipCode = vipCodeRepository.findByCodeAndUsedFalse(code)
            .orElseThrow(() -> new InvalidVipCodeException("Invalid or used VIP code"));
            
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpireDate;
        
        if (user.getVipExpireDate() == null || user.getVipExpireDate().isBefore(now)) {
            newExpireDate = now.plusDays(vipCode.getValidDays());
        } else {
            newExpireDate = user.getVipExpireDate().plusDays(vipCode.getValidDays());
        }
        
        user.setVipExpireDate(newExpireDate);
        
        // 标记兑换码为已使用
        vipCode.setUsed(true);
        vipCode.setUsedBy(userId);
        vipCode.setUsedAt(now);
        vipCodeRepository.save(vipCode);
        
        return userRepository.save(user);
    }

    @Override
    public User redeemPaymentCode(Long userId, String code) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        PaymentCode paymentCode = paymentCodeRepository.findByCodeAndUsedFalse(code)
            .orElseThrow(() -> new RuntimeException("Invalid payment code"));

        LocalDateTime now = LocalDateTime.now();

        user.setPoints(user.getPoints() + paymentCode.getPoints());
        
        // 标记兑换码为已使用
        paymentCode.setUsed(true);
        paymentCode.setUsedBy(userId);
        paymentCode.setUsedAt(now);
        paymentCodeRepository.save(paymentCode);
        
        return userRepository.save(user);
    }

        @Override
    @Transactional
    public User deductPoints(Long userId, Integer points) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (user.getPoints() < points) {
            throw new InsufficientPointsException("Insufficient points");
        }
        
        user.setPoints(user.getPoints() - points);
        return userRepository.save(user);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public String getSecurityQuestion(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getSecurityQuestion();
    }

    @Override
    public String resetPassword(String username, String securityAnswer, String newPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!securityAnswer.equals(user.getSecurityAnswer())) {
            throw new RuntimeException("Incorrect security answer");
        }

        user.setHashedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Password reset successful";
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, UserUpdateDTO updates) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (updates.getPassword() != null) {
            user.setHashedPassword(passwordEncoder.encode(updates.getPassword()));
        }
        if (updates.getUsername() != null) user.setUsername(updates.getUsername());
        if (updates.getPoints() != null) user.setPoints(updates.getPoints());
        if (updates.getVipExpireDate() != null) user.setVipExpireDate(updates.getVipExpireDate());
        if (updates.getIsAdmin() != null) user.setIsAdmin(updates.getIsAdmin());
        if (updates.getIsActive() != null) user.setIsActive(updates.getIsActive());
        
        return userRepository.save(user);
    }

    @Override
    public User deleteUser(Long id){
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
            
        userRepository.delete(user);
        return user;
    }

} 