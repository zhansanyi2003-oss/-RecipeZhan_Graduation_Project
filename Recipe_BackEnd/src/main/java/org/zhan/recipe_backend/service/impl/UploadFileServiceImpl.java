package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.zhan.recipe_backend.service.UploadFileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Service
public class UploadFileServiceImpl implements UploadFileService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    @Value("${recipe.file.upload-dir}")
    private String uploadDir;

    @Value("${recipe.file.upload-url}")
    private String urlPrefix;

    @Value("${recipe.file.max-size-bytes:5242880}")
    private long maxSizeBytes;

    @Override
    public String saveFile(MultipartFile file) throws IOException {
        validateFile(file);

        Path uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);

        String md5 = DigestUtils.md5DigestAsHex(file.getInputStream());
        String extension = extractExtension(file.getOriginalFilename());
        String newFileName = md5 + extension;
        Path destination = uploadRoot.resolve(newFileName).normalize();

        if (!destination.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        if (Files.notExists(destination)) {
            file.transferTo(destination.toFile());
        }
        return urlPrefix + newFileName;

    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a non-empty image file.");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("Image size must be 5 MB or smaller.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only JPG, PNG, or WEBP images are allowed.");
        }

        extractExtension(file.getOriginalFilename());
    }

    private String extractExtension(String originalFilename) {
        String cleanedName = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        int dotIndex = cleanedName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == cleanedName.length() - 1) {
            throw new IllegalArgumentException("File extension is required.");
        }

        String extension = cleanedName.substring(dotIndex).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Only JPG, PNG, or WEBP images are allowed.");
        }
        return extension;
    }
}
