package org.tokomoapp.tokomo_be.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String hashedPassword;
    
    @Column(name = "security_question", nullable = false)
    private String securityQuestion;
    
    @Column(nullable = false)
    private String securityAnswer;
    
    @Column(name = "points")
    private Integer points;

    @Column(name = "vip_expire_date")
    private LocalDateTime vipExpireDate;

    @Column(name = "is_admin")
    private Boolean isAdmin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login_at") 
    private LocalDateTime lastLoginAt;

    @Column(name = "is_active")
    private Boolean isActive;
    

    public User(String username, String hashedPassword, String securityQuestion, String securityAnswer) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.points = 0;
        this.isAdmin = false;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
        this.isActive = true;
    }

    public boolean isVIP() {
        return vipExpireDate != null && vipExpireDate.isAfter(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", hashedPassword='" + "********" + '\'' +
                ", securityQuestion='" + securityQuestion + '\'' +
                '}';
    }

    public User sanitize() {
        User sanitizedUser = new User();
        sanitizedUser.setId(this.getId());
        sanitizedUser.setUsername(this.getUsername());
        sanitizedUser.setPoints(this.getPoints());
        sanitizedUser.setVipExpireDate(this.getVipExpireDate());
        sanitizedUser.setIsAdmin(this.getIsAdmin());
        sanitizedUser.setCreatedAt(this.getCreatedAt());
        sanitizedUser.setLastLoginAt(this.getLastLoginAt());
        sanitizedUser.setIsActive(this.getIsActive());
        sanitizedUser.setSecurityQuestion(this.getSecurityQuestion());
        sanitizedUser.setHashedPassword("********");
        sanitizedUser.setSecurityAnswer("********");
        return sanitizedUser;
    }

    public boolean hasRole(String role) {
        return isAdmin != null && isAdmin && role.equals("ADMIN");
    }
}
