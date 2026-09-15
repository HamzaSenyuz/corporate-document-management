package com.hamza.document_management.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private boolean archived = false;

    private Instant archivedAt;

    // Bir dokümanı bir user yükler — Many-to-One ilişki
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Document() {
    }

    public Document(String title, String description, String category, User createdBy) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.createdBy = createdBy;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public void archive() {
        this.archived = true;
        this.archivedAt = Instant.now();
    }

    public void unarchive() {
        this.archived = false;
        this.archivedAt = null;
    }

    // --- Getters (setter'ları ihtiyaç oldukça ekle) ---

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isArchived() { return archived; }
    public Instant getArchivedAt() { return archivedAt; }
    public User getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}