package org.tokomoapp.tokomo_be.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserUpdateDTO {
    private String username;
    private String password;  // 明文密码
    private Integer points;
    private LocalDateTime vipExpireDate;
    private Boolean isAdmin;
    private Boolean isActive;
} 