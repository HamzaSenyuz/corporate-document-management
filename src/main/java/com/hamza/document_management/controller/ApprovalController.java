package com.hamza.document_management.controller;

import com.hamza.document_management.entity.Decision;
import com.hamza.document_management.entity.User;
import com.hamza.document_management.entity.VersionStatus;
import com.hamza.document_management.repository.DocumentVersionRepository;
import com.hamza.document_management.repository.UserRepository;
import com.hamza.document_management.service.DocumentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/approvals")
public class ApprovalController {

    private final DocumentService documentService;
    private final DocumentVersionRepository versionRepository;
    private final UserRepository userRepository;

    public ApprovalController(DocumentService documentService,
                              DocumentVersionRepository versionRepository,
                              UserRepository userRepository) {
        this.documentService = documentService;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
    }

    /** GET /approvals -> onay bekleyenler listesi */
    @GetMapping
    public String pending(Model model) {
        model.addAttribute("versions",
                versionRepository.findByStatus(VersionStatus.PENDING_REVIEW));
        return "approvals";
    }

    @PostMapping("/{versionId}")
    public String decide(@PathVariable Long versionId,
                         @RequestParam String decision,
                         @RequestParam(required = false) String comment,
                         Principal principal) {

        User manager = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Kullanici bulunamadi"));

        documentService.review(versionId, Decision.valueOf(decision), comment, manager);

        return "redirect:/approvals";
    }
}