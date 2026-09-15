package com.hamza.document_management.repository;

import com.hamza.document_management.entity.Document;
import com.hamza.document_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Bir kullanıcının kendi dokümanları (Employee kendi listesini görecek)
    List<Document> findByCreatedByAndArchivedFalse(User user);

    // Arşivlenmiş dokümanlar (ayrı sekmede gösterilecek)
    List<Document> findByCreatedByAndArchivedTrue(User user);

    // Tüm aktif dokümanlar (Manager/Admin için)
    List<Document> findByArchivedFalse();
}