package com.hamza.document_management.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "approvals")
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private DocumentVersion version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewed_by", nullable = false)
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Decision decision;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false, updatable = false)
    private Instant reviewedAt;

    protected Approval() {
    }

    public Approval(DocumentVersion version, User reviewedBy, Decision decision, String comment) {
        this.version = version;
        this.reviewedBy = reviewedBy;
        this.decision = decision;
        this.comment = comment;
    }

    @PrePersist
    protected void onCreate() {
        this.reviewedAt = Instant.now();
    }

    public Long getId() { return id; }
    public DocumentVersion getVersion() { return version; }
    public User getReviewedBy() { return reviewedBy; }
    public Decision getDecision() { return decision; }
    public String getComment() { return comment; }
    public Instant getReviewedAt() { return reviewedAt; }
}