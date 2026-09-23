package com.seyahood.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String username;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    @JsonIgnore
    private String passwordHash;

    private String firstName;
    private String lastName;
    private String bio;
    private String profileImageUrl;
    private String location;
    private String website;
    private String favoriteDestination;
    private String coverColor;

    @Column(name = "is_verified")
    private Boolean verified = false;

    @JsonIgnore
    private String verificationCode;

    @JsonIgnore
    private LocalDateTime verificationCodeExpiry;

    private LocalDateTime createdAt;

    private String resetPasswordCode;
    private LocalDateTime resetPasswordCodeExpiry;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan")
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    private LocalDateTime subscriptionExpiresAt;
    private String revenueCatCustomerId;

    public enum SubscriptionPlan {
        FREE, PLUS, PRO
    }

    public boolean isPro() {
        return subscriptionPlan == SubscriptionPlan.PRO &&
                (subscriptionExpiresAt == null ||
                        subscriptionExpiresAt.isAfter(LocalDateTime.now()));
    }

    public boolean isPlus() {
        return (subscriptionPlan == SubscriptionPlan.PLUS ||
                subscriptionPlan == SubscriptionPlan.PRO) &&
                (subscriptionExpiresAt == null ||
                        subscriptionExpiresAt.isAfter(LocalDateTime.now()));
    }

    public int getJournalLimit() {
        if (isPro()) return Integer.MAX_VALUE;
        if (isPlus()) return 20;
        return 5;
    }

    public int getPageLimit() {
        if (isPro()) return Integer.MAX_VALUE;
        if (isPlus()) return 40;
        return 15;
    }
}