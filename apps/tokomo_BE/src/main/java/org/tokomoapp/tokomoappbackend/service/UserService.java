package org.tokomoapp.tokomoappbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tokomoapp.tokomoappbackend.model.SecurityQuestion;
import org.tokomoapp.tokomoappbackend.model.User;
import org.tokomoapp.tokomoappbackend.repository.SecurityQuestionRepository;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final JwtUtil jwtUtil;
    private final VipCodeRepository vipCodeRepository;

    @Autowired
    public UserService(UserRepository userRepository, SecurityQuestionRepository securityQuestionRepository, JwtUtil jwtUtil, VipCodeRepository vipCodeRepository) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userRepository = userRepository;
        this.securityQuestionRepository = securityQuestionRepository;
        this.jwtUtil = jwtUtil;
        this.vipCodeRepository = vipCodeRepository;
    }


    @Transactional
    public User registerUser(String username, String password, String question, String answer, String vipCode) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        // 如果提供了VIP码且不为空字符串，先验证VIP码
        VipCode vipCodeEntity = null;
        if (vipCode != null && !vipCode.trim().isEmpty()) {
            try {
                vipCodeEntity = vipCodeRepository.findByCodeAndUsedFalse(vipCode)
                    .orElseThrow(() -> new RuntimeException("Invalid or used VIP code"));
            } catch (Exception e) {
                throw new RuntimeException("Error validating VIP code: " + e.getMessage());
            }
        }

        // 创建安全问题
        SecurityQuestion securityQuestion = new SecurityQuestion();
        securityQuestion.setQuestion(question);
        securityQuestion.setAnswer(answer);
        SecurityQuestion savedQuestion = securityQuestionRepository.save(securityQuestion);

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setHashedPassword(passwordEncoder.encode(password));
        user.setSecurityQuestionId(savedQuestion.getId());
        user.setPoints(0);
        user.setVipExpireDate(null);
        user.setIsAdmin(false);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLoginAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // 如果有有效的VIP码，激活VIP
        if (vipCodeEntity != null) {
            try {
                LocalDateTime expireDate = LocalDateTime.now().plusDays(vipCodeEntity.getValidDays());
                savedUser.setVipExpireDate(expireDate);
                
                // 标记VIP码为已使用
                vipCodeEntity.setUsed(true);
                vipCodeEntity.setUsedBy(savedUser.getId());
                vipCodeEntity.setUsedAt(LocalDateTime.now());
                vipCodeRepository.save(vipCodeEntity);
                
                savedUser = userRepository.save(savedUser);
            } catch (Exception e) {
                throw new RuntimeException("Error activating VIP: " + e.getMessage());
            }
        }

        return savedUser;
    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public SecurityQuestion findSecurityQuestionById(Long id) {
        return securityQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("安全问题不存在"));
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

    @Transactional
    public User deductPoints(long userId, int points) {
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

    public User getUserById(Long userId) {
        return findById(userId)
            .map(User::sanitize)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    @Transactional
    public User updateUser(Long userId, Map<String, Object> updates) {
        User user = getUserById(userId);
        
        // 更新用户信息
        if (updates.containsKey("username")) {
            String newUsername = (String) updates.get("username");
            if (userRepository.existsByUsername(newUsername) && !user.getUsername().equals(newUsername)) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(newUsername);
        }
        
        if (updates.containsKey("points")) {
            user.setPoints((Integer) updates.get("points"));
        }
        
        if (updates.containsKey("isAdmin")) {
            user.setIsAdmin((Boolean) updates.get("isAdmin"));
        }
        
        if (updates.containsKey("isActive")) {
            user.setIsActive((Boolean) updates.get("isActive"));
        }
        
        if (updates.containsKey("vipExpireDate")) {
            // 假设日期格式为ISO-8601字符串
            String dateStr = (String) updates.get("vipExpireDate");
            user.setVipExpireDate(dateStr != null ? LocalDateTime.parse(dateStr) : null);
        }
        
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public SecurityQuestion getSecurityQuestion(Long questionId) {
        return securityQuestionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Security question not found"));
    }

    @Transactional
    public void resetPassword(String username, String answer, String newPassword) {
        User user = findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        SecurityQuestion securityQuestion = getSecurityQuestion(user.getSecurityQuestionId());
        
        if (!securityQuestion.getAnswer().equals(answer)) {
            throw new RuntimeException("Incorrect security answer");
        }
        
        user.setHashedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}