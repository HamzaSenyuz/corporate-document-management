package com.hamza.document_management.repository;

import com.hamza.document_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Login için: email ile user'ı bul
    Optional<User> findByEmail(String email);

    // Kayıt sırasında: bu email zaten kullanılıyor mu?
    boolean existsByEmail(String email);
}