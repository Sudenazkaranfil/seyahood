package com.seyahood.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes")
@Data
public class PromoCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private User.SubscriptionPlan plan;

    private int durationDays;
    private int maxRedemptions = 1;
    private int timesRedeemed = 0;
    private boolean active = true;

    private LocalDateTime expiresAt;
    private String note;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
