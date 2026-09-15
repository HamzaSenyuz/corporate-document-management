package com.hamza.document_management.repository;

import com.hamza.document_management.entity.Approval;
import com.hamza.document_management.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    // Bir dokümana ait tüm kararlar (en yeni önce)
    List<Approval> findByVersion_DocumentOrderByReviewedAtDesc(Document document);
}