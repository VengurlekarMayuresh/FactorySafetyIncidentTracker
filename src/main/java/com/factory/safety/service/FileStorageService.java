package com.factory.safety.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    private Cloudinary cloudinary;

    public FileStorageService() {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory at " + uploadDir, e);
        }
    }

    @PostConstruct
    public void initCloudinary() {
        if (cloudName != null && !cloudName.trim().isEmpty() &&
            apiKey != null && !apiKey.trim().isEmpty() &&
            apiSecret != null && !apiSecret.trim().isEmpty()) {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName.trim(),
                "api_key", apiKey.trim(),
                "api_secret", apiSecret.trim(),
                "secure", true
            ));
            System.out.println(" Cloudinary photo storage service initialized successfully for: " + cloudName);
        } else {
            System.out.println("ℹ Cloudinary credentials not detected. Falling back to local disk storage in ./uploads/");
        }
    }

    public boolean isCloudinaryConfigured() {
        return cloudinary != null;
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Try Cloudinary first if configured
        if (cloudinary != null) {
            try {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "factory_safety_incidents",
                    "resource_type", "image"
                ));
                String secureUrl = (String) uploadResult.get("secure_url");
                if (secureUrl != null) {
                    return secureUrl;
                }
            } catch (Exception e) {
                System.err.println("Cloudinary upload failed, falling back to local disk: " + e.getMessage());
            }
        }

        // Fallback to local disk storage
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                extension = ".jpg";
            }

            String newFilename = UUID.randomUUID().toString() + extension;
            Path targetLocation = uploadDir.resolve(newFilename);
            file.transferTo(targetLocation.toFile());

            return "/uploads/" + newFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file on disk", e);
        }
    }

    public String storeBase64Image(String base64Data) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            return null;
        }

        // Try Cloudinary first if configured
        if (cloudinary != null) {
            try {
                // Cloudinary natively supports data URIs e.g. "data:image/jpeg;base64,..."
                String dataUri = base64Data.trim();
                if (!dataUri.startsWith("data:")) {
                    dataUri = "data:image/jpeg;base64," + dataUri;
                }
                Map<?, ?> uploadResult = cloudinary.uploader().upload(dataUri, ObjectUtils.asMap(
                    "folder", "factory_safety_incidents",
                    "resource_type", "image"
                ));
                String secureUrl = (String) uploadResult.get("secure_url");
                if (secureUrl != null) {
                    return secureUrl;
                }
            } catch (Exception e) {
                System.err.println("Cloudinary base64 upload failed, falling back to local disk: " + e.getMessage());
            }
        }

        // Fallback to local disk storage
        try {
            String base64Image = base64Data;
            String extension = ".jpg";
            if (base64Data.contains(",")) {
                String header = base64Data.substring(0, base64Data.indexOf(","));
                if (header.contains("png")) {
                    extension = ".png";
                } else if (header.contains("webp")) {
                    extension = ".webp";
                }
                base64Image = base64Data.substring(base64Data.indexOf(",") + 1);
            }

            byte[] decodedBytes = Base64.getDecoder().decode(base64Image.trim());
            String newFilename = UUID.randomUUID().toString() + extension;
            Path targetLocation = uploadDir.resolve(newFilename);

            try (FileOutputStream fos = new FileOutputStream(targetLocation.toFile())) {
                fos.write(decodedBytes);
            }

            return "/uploads/" + newFilename;
        } catch (Exception e) {
            System.err.println("Failed to decode base64 image: " + e.getMessage());
            return null;
        }
    }

    public Path getUploadDir() {
        return uploadDir;
    }
}
