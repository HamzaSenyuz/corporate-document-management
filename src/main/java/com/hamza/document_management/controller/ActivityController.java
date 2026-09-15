package com.hamza.document_management.controller;

import com.hamza.document_management.repository.ActivityLogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ActivityController {

    private final ActivityLogRepository activityLogRepository;

    public ActivityController(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @GetMapping("/activity")
    public String activity(Model model) {
        model.addAttribute("logs", activityLogRepository.findTop50ByOrderByCreatedAtDesc());
        return "activity";
    }
}