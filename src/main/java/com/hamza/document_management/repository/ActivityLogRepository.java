package com.hamza.document_management.repository;

import com.hamza.document_management.entity.ActivityLog;
import com.hamza.document_management.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    void deleteByDocument(Document document);

    // Bir dokümanın geçmişi (en yeni önce)
    List<ActivityLog> findByDocumentOrderByCreatedAtDesc(Document document);

    // Tüm sistemdeki son 50 hareket
    List<ActivityLog> findTop50ByOrderByCreatedAtDesc();
}