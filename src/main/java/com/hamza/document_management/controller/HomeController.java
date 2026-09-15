package com.hamza.document_management.controller;

import com.hamza.document_management.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserRepository userRepository;

    // Constructor injection: Spring bu controller'ı oluştururken
    // UserRepository'yi otomatik parametre olarak veriyor
    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        long count = userRepository.count();      // DB'ye SELECT COUNT(*) FROM users
        model.addAttribute("userCount", count);   // HTML'e "userCount" adıyla gönder
        return "home";                            // "home.html" template'ini render et
    }
}