package org.tokomoapp.tokomoappbackend.model;

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
    
    @Column(name = "security_question_id")
    private Long securityQuestionId;
    
    @Column(name = "Points")
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
    

    public User(Long id, String username, String hashedPassword, Long securityQuestionId) {
        this.id = id;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.securityQuestionId = securityQuestionId;
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
                ", securityQuestionId=" + securityQuestionId +
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
        sanitizedUser.setSecurityQuestionId(this.getSecurityQuestionId());
        sanitizedUser.setHashedPassword("********");
        return sanitizedUser;
    }
}
