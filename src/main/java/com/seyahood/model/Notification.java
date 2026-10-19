package com.seyahood.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "email", "verificationCode", "verificationCodeExpiry", "resetPasswordCode", "resetPasswordCodeExpiry"})
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "email", "verificationCode", "verificationCodeExpiry", "resetPasswordCode", "resetPasswordCodeExpiry"})
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @ManyToOne
    @JoinColumn(name = "journal_id")
    private Journal journal;

    @Column(nullable = false)
    private boolean read = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Type {
        FOLLOW, JOURNAL_SAVE
    }
}
