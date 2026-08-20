package com.dpc.smart_staffing_backend.service;

import com.dpc.smart_staffing_backend.exception.InvalidCvException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class CvStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final Path storageDirectory;

    public CvStorageService(@Value("${cv.storage.location:uploads/cvs}") String storageLocation) {
        this.storageDirectory = Path.of(storageLocation).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCvException("A CV file is required");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidCvException("CV must be a PDF, DOC, or DOCX file");
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!Set.of("pdf", "doc", "docx").contains(extension)) {
            throw new InvalidCvException("CV must have a PDF, DOC, or DOCX extension");
        }

        try {
            Files.createDirectories(storageDirectory);
            String storedFileName = UUID.randomUUID() + "." + extension;
            Files.copy(file.getInputStream(), storageDirectory.resolve(storedFileName), StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store CV file", exception);
        }
    }

    public Resource load(String storedFileName) {
        Path file = storageDirectory.resolve(storedFileName).normalize();
        if (!file.startsWith(storageDirectory) || !Files.isRegularFile(file)) {
            throw new com.dpc.smart_staffing_backend.exception.ResourceNotFoundException("CV file was not found");
        }
        return new FileSystemResource(file);
    }

    public void delete(String storedFileName) {
        try {
            Files.deleteIfExists(storageDirectory.resolve(storedFileName).normalize());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not remove CV file", exception);
        }
    }

    private String extensionOf(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
