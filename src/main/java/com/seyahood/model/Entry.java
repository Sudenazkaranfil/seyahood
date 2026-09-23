package com.seyahood.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entries")
@Data
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "journal_id", nullable = false)
    private Journal journal;

    private LocalDate date;
    private String locationName;
    private Double lat;
    private Double lng;

    @Column(columnDefinition = "TEXT")
    private String textContent;

    private String mood;

    @Column(columnDefinition = "TEXT")
    private String canvasData;

    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "entry_photos", joinColumns = @JoinColumn(name = "entry_id"))
    @Column(name = "photo_url")
    private List<String> photoUrls = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
