package org.tokomoapp.tokomo_be.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "vip_codes")
@Data
public class VipCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private Integer validDays;

    @Column(nullable = false)
    private boolean used = false;

    private Long usedBy;
    
    private LocalDateTime usedAt;

} 