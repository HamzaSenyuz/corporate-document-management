package com.hamza.document_management.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityAction action;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected ActivityLog() {
    }

    public ActivityLog(Document document, User user, ActivityAction action, String description) {
        this.document = document;
        this.user = user;
        this.action = action;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    /** Ekranda okunabilir tarih: 15.09.2026 14:32 */
    public String getFormattedDate() {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(createdAt);
    }

    public Long getId() { return id; }
    public Document getDocument() { return document; }
    public User getUser() { return user; }
    public ActivityAction getAction() { return action; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}