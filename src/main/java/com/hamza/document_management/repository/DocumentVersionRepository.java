package com.hamza.document_management.repository;

import com.hamza.document_management.entity.Document;
import com.hamza.document_management.entity.DocumentVersion;
import com.hamza.document_management.entity.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    long countByDocument(Document document);

    // Bir dokümanın tüm versiyonlarını sırayla getir (V1, V2, V3...)
    List<DocumentVersion> findByDocumentOrderByVersionNumberAsc(Document document);

    // Bir dokümanın en son versiyonu (üzerinde çalışılan)
    Optional<DocumentVersion> findTopByDocumentOrderByVersionNumberDesc(Document document);

    // Bir dokümanın en son onaylı versiyonu (resmî sürüm)
    Optional<DocumentVersion> findTopByDocumentAndStatusOrderByVersionNumberDesc(
            Document document, VersionStatus status);

    // Onay bekleyen tüm versiyonlar (manager dashboard'ı için)
    List<DocumentVersion> findByStatus(VersionStatus status);

}