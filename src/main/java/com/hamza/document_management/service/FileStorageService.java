package com.hamza.document_management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDirPath) {
        this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);   // klasör yoksa oluştur
        } catch (IOException e) {
            throw new RuntimeException("Upload klasoru olusturulamadi: " + this.uploadDir, e);
        }
    }

    /**
     * Dosyayı diske kaydeder, diskteki yeni adını (UUID) döndürür.
     */
    public String store(MultipartFile file) {
        String originalName = file.getOriginalFilename();

        // Uzantıyı ayıkla: "rapor.pdf" -> ".pdf"
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        // Benzersiz isim üret: "a3f9c1e2-8b44-....pdf"
        String storedName = UUID.randomUUID() + extension;

        Path target = uploadDir.resolve(storedName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Dosya kaydedilemedi: " + originalName, e);
        }

        return storedName;
    }
    public void delete(String storedFileName) {
        try {
            Files.deleteIfExists(uploadDir.resolve(storedFileName));
        } catch (IOException e) {
            System.err.println("Dosya silinemedi: " + storedFileName);
        }
    }
}