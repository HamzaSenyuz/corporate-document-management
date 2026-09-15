package com.hamza.document_management.config;

import com.hamza.document_management.entity.Role;
import com.hamza.document_management.entity.User;
import com.hamza.document_management.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.save(new User("Ahmet Çalışan", "employee@test.com",
                passwordEncoder.encode("1234"), Role.EMPLOYEE));

        userRepository.save(new User("Mehmet Yönetici", "manager@test.com",
                passwordEncoder.encode("1234"), Role.MANAGER));

        System.out.println(">>> Test kullanicilari olusturuldu (sifre: 1234)");
    }
}