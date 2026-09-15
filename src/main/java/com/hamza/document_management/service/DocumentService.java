package com.hamza.document_management.service;

import com.hamza.document_management.entity.*;
import com.hamza.document_management.repository.ActivityLogRepository;
import com.hamza.document_management.repository.DocumentRepository;
import com.hamza.document_management.repository.DocumentVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.hamza.document_management.repository.ApprovalRepository;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;
    private final FileStorageService fileStorageService;
    private final ApprovalRepository approvalRepository;
    private final ActivityLogRepository activityLogRepository;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentVersionRepository versionRepository,
                           FileStorageService fileStorageService, ApprovalRepository approvalRepository, ActivityLogRepository activityLogRepository) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.fileStorageService = fileStorageService;
        this.approvalRepository = approvalRepository;
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public Document createDocument(String title, String description, String category,
                                   MultipartFile file, User uploader) {

        // 1) Dosyayı diske yaz
        String storedName = fileStorageService.store(file);

        // 2) Document kaydını oluştur
        Document document = new Document(title, description, category, uploader);
        documentRepository.save(document);

        // 3) İlk versiyonu (V1) oluştur
        DocumentVersion version = new DocumentVersion(
                document,
                1,                              // ilk versiyon
                file.getOriginalFilename(),     // "rapor.pdf"
                storedName,                     // "a3f9c1e2-....pdf"
                file.getContentType(),          // "application/pdf"
                file.getSize(),                 // byte cinsinden
                uploader
        );
        versionRepository.save(version);

        log(document, uploader, ActivityAction.DOCUMENT_CREATED,
                "Doküman oluşturuldu (V1: " + file.getOriginalFilename() + ")");

        return document;
    }
    /** Taslağı onaya gönder: DRAFT -> PENDING_REVIEW */
    @Transactional
    public void submitForReview(Long versionId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Versiyon bulunamadi"));

        version.setStatus(VersionStatus.PENDING_REVIEW);
        versionRepository.save(version);

        log(version.getDocument(), version.getCreatedBy(), ActivityAction.SUBMITTED_FOR_REVIEW,
                "V" + version.getVersionNumber() + " onaya gönderildi");
    }

    /** Yönetici kararı: PENDING_REVIEW -> APPROVED veya REJECTED */
    @Transactional
    public void review(Long versionId, Decision decision, String comment, User reviewer) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Versiyon bulunamadi"));

        if (decision == Decision.APPROVED) {
            version.setStatus(VersionStatus.APPROVED);
        } else {
            version.setStatus(VersionStatus.REJECTED);
        }
        versionRepository.save(version);

        approvalRepository.save(new Approval(version, reviewer, decision, comment));

        if (decision == Decision.APPROVED) {
            log(version.getDocument(), reviewer, ActivityAction.APPROVED,
                    "V" + version.getVersionNumber() + " onaylandı");
        } else {
            log(version.getDocument(), reviewer, ActivityAction.REJECTED,
                    "V" + version.getVersionNumber() + " reddedildi: " + comment);
        }
    }

    /** Mevcut dokümana yeni versiyon ekle (V2, V3...) */
    @Transactional
    public void addVersion(Long documentId, MultipartFile file, User uploader) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Dokuman bulunamadi"));

        // Son versiyon numarasını bul, bir fazlasını kullan
        int nextNumber = versionRepository
                .findTopByDocumentOrderByVersionNumberDesc(document)
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        String storedName = fileStorageService.store(file);

        versionRepository.save(new DocumentVersion(
                document, nextNumber,
                file.getOriginalFilename(), storedName,
                file.getContentType(), file.getSize(), uploader
        ));
        log(document, uploader, ActivityAction.VERSION_UPLOADED,
                "V" + nextNumber + " yüklendi (" + file.getOriginalFilename() + ")");
    }
    private void log(Document document, User user, ActivityAction action, String description) {
        activityLogRepository.save(new ActivityLog(document, user, action, description));
    }

    @Transactional
    public void deleteVersion(Long versionId, User currentUser) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Versiyon bulunamadi"));

        // Kural 1: sadece taslak silinebilir
        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new RuntimeException("Sadece taslak versiyonlar silinebilir");
        }

        // Kural 2: sadece kendi yüklediğini silebilir
        if (!version.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Sadece kendi yukledigin versiyonu silebilirsin");
        }

        Document document = version.getDocument();

        // Diskteki dosyayı sil
        fileStorageService.delete(version.getStoredFileName());

        // Veritabanı kaydını sil
        versionRepository.delete(version);

        // Bu son versiyondu ise dokümanın kendisini de sil
        if (versionRepository.countByDocument(document) == 0) {
            activityLogRepository.deleteByDocument(document);
            documentRepository.delete(document);
        } else {
            log(document, currentUser, ActivityAction.VERSION_UPLOADED,
                    "V" + version.getVersionNumber() + " silindi");
        }
    }
}