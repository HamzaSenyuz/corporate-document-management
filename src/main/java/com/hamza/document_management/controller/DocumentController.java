package com.hamza.document_management.controller;

import com.hamza.document_management.entity.Document;
import com.hamza.document_management.entity.DocumentVersion;
import com.hamza.document_management.entity.User;
import com.hamza.document_management.repository.ActivityLogRepository;
import com.hamza.document_management.repository.ApprovalRepository;
import com.hamza.document_management.repository.DocumentRepository;
import com.hamza.document_management.repository.DocumentVersionRepository;
import com.hamza.document_management.repository.UserRepository;
import com.hamza.document_management.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;

@Controller
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;
    private final ApprovalRepository approvalRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public DocumentController(DocumentService documentService,
                              DocumentRepository documentRepository,
                              DocumentVersionRepository versionRepository,
                              ApprovalRepository approvalRepository,
                              ActivityLogRepository activityLogRepository,
                              UserRepository userRepository) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.approvalRepository = approvalRepository;
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    /** GET /documents -> doküman listesi */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("documents", documentRepository.findByArchivedFalse());
        return "documents";
    }

    /** GET /documents/upload -> yeni doküman formu */
    @GetMapping("/upload")
    public String uploadForm() {
        return "upload";
    }

    /** POST /documents/upload -> yeni doküman kaydet */
    @PostMapping("/upload")
    public String upload(@RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam String category,
                         @RequestParam("file") MultipartFile file,
                         Principal principal) {

        documentService.createDocument(title, description, category, file, currentUser(principal));
        return "redirect:/documents";
    }

    /** GET /documents/5 -> doküman detayı */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dokuman bulunamadi"));

        model.addAttribute("document", document);
        model.addAttribute("versions",
                versionRepository.findByDocumentOrderByVersionNumberAsc(document));
        model.addAttribute("approvals",
                approvalRepository.findByVersion_DocumentOrderByReviewedAtDesc(document));
        model.addAttribute("logs",
                activityLogRepository.findByDocumentOrderByCreatedAtDesc(document));

        return "document-detail";
    }

    /** POST /documents/5/versions/12/submit -> onaya gönder */
    @PostMapping("/{docId}/versions/{versionId}/submit")
    public String submit(@PathVariable Long docId, @PathVariable Long versionId) {
        documentService.submitForReview(versionId);
        return "redirect:/documents/" + docId;
    }

    /** POST /documents/5/versions -> yeni versiyon yükle */
    @PostMapping("/{docId}/versions")
    public String addVersion(@PathVariable Long docId,
                             @RequestParam("file") MultipartFile file,
                             Principal principal) {

        documentService.addVersion(docId, file, currentUser(principal));
        return "redirect:/documents/" + docId;
    }

    /** GET /documents/versions/12/download -> dosyayı indir */
    @GetMapping("/versions/{versionId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long versionId) throws Exception {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Versiyon bulunamadi"));

        Path path = Paths.get("./uploads").resolve(version.getStoredFileName());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(version.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + version.getOriginalFileName() + "\"")
                .body(resource);
    }
    /** GET /documents/archived -> arşivlenmiş dokümanlar */
    @GetMapping("/archived")
    public String archived(Model model) {
        model.addAttribute("documents", documentRepository.findByArchivedTrue());
        return "archived";
    }

    /** POST /documents/5/archive */
    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            documentService.archiveDocument(id, currentUser(principal));
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/documents";
    }

    /** POST /documents/5/unarchive */
    @PostMapping("/{id}/unarchive")
    public String unarchive(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            documentService.unarchiveDocument(id, currentUser(principal));
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/documents/archived";
    }

    /** Giriş yapmış kullanıcıyı veritabanından getirir */
    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Kullanici bulunamadi"));
    }

    /** POST /documents/5/versions/12/delete -> taslak versiyonu sil */
    @PostMapping("/{docId}/versions/{versionId}/delete")
    public String deleteVersion(@PathVariable Long docId,
                                @PathVariable Long versionId,
                                Principal principal) {

        documentService.deleteVersion(versionId, currentUser(principal));
        return "redirect:/documents";
    }
}