package org.tokomoapp.tokomoappbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vip_codes")
public class VipCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    @Column(nullable = false)
    private Integer validDays;
    
    @Column(nullable = false)
    private Boolean used = false;
    
    @Column(name = "used_by")
    private Long usedBy;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
} 