package org.zhan.recipe_backend.service.impl;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

final class ImageUploadSupport {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    private ImageUploadSupport() {
    }

    static String buildStoredFileName(MultipartFile file, long maxSizeBytes) throws IOException {
        validateFile(file, maxSizeBytes);
        String md5 = DigestUtils.md5DigestAsHex(file.getInputStream());
        return md5 + extractExtension(file.getOriginalFilename());
    }

    private static void validateFile(MultipartFile file, long maxSizeBytes) {
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

    private static String extractExtension(String originalFilename) {
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
