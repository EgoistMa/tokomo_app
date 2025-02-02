package org.tokomoapp.tokomoappbackend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tokomoapp.tokomoappbackend.model.User;
import org.tokomoapp.tokomoappbackend.repository.UserRepository;
import org.tokomoapp.tokomoappbackend.util.JwtUtil;
import org.tokomoapp.tokomoappbackend.exception.UserAlreadyExistsException;
import org.tokomoapp.tokomoappbackend.exception.InsufficientPointsException;
import java.time.LocalDateTime;
import org.tokomoapp.tokomoappbackend.repository.VipCodeRepository;
import org.tokomoapp.tokomoappbackend.model.VipCode;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import org.tokomoapp.tokomoappbackend.exception.InvalidVipCodeException;
import java.util.stream.Collectors;
import org.tokomoapp.tokomoappbackend.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final VipCodeRepository vipCodeRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, VipCodeRepository vipCodeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.vipCodeRepository = vipCodeRepository;
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
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"))
                .sanitize();
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

    @Transactional
    public String login(String username, String password) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Invalid username"));

            if (!passwordEncoder.matches(password, user.getHashedPassword())) {
                throw new RuntimeException("Invalid password");
            }
            
            // 生成 token
            String token = jwtUtil.generateToken(user);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("internal error");
        }
    }

    @Transactional
    public User addPoints(long userId, int points) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        user.setPoints(user.getPoints() + points);
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

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        return user;
    }

    @Transactional
    public List<VipCode> generateVipCodes(int amount, int validDays) {
        List<VipCode> codes = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            String code;
            do {
                code = generateRandomVipCode();
            } while (vipCodeRepository.existsByCode(code));
            
            VipCode vipCode = new VipCode();
            vipCode.setCode(code);
            vipCode.setValidDays(validDays);
            vipCode.setUsed(false);
            codes.add(vipCodeRepository.save(vipCode));
        }
        return codes;
    }
    
    private String generateRandomVipCode() {
        return "VIP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
            .map(User::sanitize)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public User updateUser(Long userId, User updates) {
        User user = getUserById(userId);
        
        if (updates.getPoints() != null) {
            user.setPoints(updates.getPoints());
        }
        if (updates.getVipExpireDate() != null) {
            user.setVipExpireDate(updates.getVipExpireDate());
        }
        if (updates.getIsActive() != null) {
            user.setIsActive(updates.getIsActive());
        }
        
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"))
            .sanitize();
    }

    @Override
    public User loginUser(String username, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loginUser'");
    }

    @Override
    public void updateUserStatus(Long id, boolean isActive) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUserStatus'");
    }

    @Override
    public User addPoints(Long userId, Integer points) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addPoints'");
    }
}