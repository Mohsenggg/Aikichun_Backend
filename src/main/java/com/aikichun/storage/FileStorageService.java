package com.aikichun.storage;

import com.aikichun.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    private final String uploadDir;

    public FileStorageService(@Value("${aikichun.storage.upload-dir}") String uploadDir) {
        this.uploadDir = uploadDir;
        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new FileStorageException("Failed to create upload directory", e);
            }
        }
    }

    public String storeFile(MultipartFile file, String fileType) {
        if (file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new FileStorageException("Invalid file type for " + fileType + ": only JPEG and PNG allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new FileStorageException("File size for " + fileType + " exceeds 5 MB limit");
        }

        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath);
            return filePath.toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to store " + fileType, e);
        }
    }
}

