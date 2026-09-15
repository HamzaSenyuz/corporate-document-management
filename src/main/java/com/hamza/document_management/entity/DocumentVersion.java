package com.hamza.document_management.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "document_versions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_doc_version_number",
                columnNames = {"document_id", "version_number"}
        )
)
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false, unique = true)
    private String storedFileName;      // UUID isim, ör: "a3f9c1e2-....pdf"

    @Column(nullable = false)
    private String contentType;         // ör: "application/pdf"

    @Column(nullable = false)
    private Long fileSize;              // byte cinsinden

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VersionStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected DocumentVersion() {
    }

    public DocumentVersion(Document document, Integer versionNumber,
                           String originalFileName, String storedFileName,
                           String contentType, Long fileSize, User createdBy) {
        this.document = document;
        this.versionNumber = versionNumber;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.createdBy = createdBy;
        this.status = VersionStatus.DRAFT;   // yeni versiyon her zaman DRAFT başlar
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // --- Getters ---

    public Long getId() { return id; }
    public Document getDocument() { return document; }
    public Integer getVersionNumber() { return versionNumber; }
    public String getOriginalFileName() { return originalFileName; }
    public String getStoredFileName() { return storedFileName; }
    public String getContentType() { return contentType; }
    public Long getFileSize() { return fileSize; }
    public VersionStatus getStatus() { return status; }
    public void setStatus(VersionStatus status) { this.status = status; }
    public User getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}