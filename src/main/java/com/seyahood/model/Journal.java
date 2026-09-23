package com.seyahood.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journals")
@Data
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PRIVATE;

    private Integer viewCount = 0;
    private Integer saveCount = 0;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Visibility {
        PRIVATE, PUBLIC
    }

    @JsonIgnore
    @OneToMany(mappedBy = "journal", fetch = FetchType.LAZY)
    private List<Entry> entries = new ArrayList<>();
}