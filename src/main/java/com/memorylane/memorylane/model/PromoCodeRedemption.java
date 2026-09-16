package com.memorylane.memorylane.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_code_redemptions")
@Data
public class PromoCodeRedemption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "promo_code_id")
    private PromoCode promoCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime redeemedAt;

    @PrePersist
    protected void onCreate() {
        redeemedAt = LocalDateTime.now();
    }
}
