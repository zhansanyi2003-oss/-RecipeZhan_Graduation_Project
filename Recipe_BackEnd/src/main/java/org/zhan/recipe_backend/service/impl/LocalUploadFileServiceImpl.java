package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zhan.recipe_backend.service.UploadFileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@ConditionalOnProperty(name = "recipe.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalUploadFileServiceImpl implements UploadFileService {

    @Value("${recipe.file.upload-dir}")
    private String uploadDir;

    @Value("${recipe.file.upload-url}")
    private String urlPrefix;

    @Value("${recipe.file.max-size-bytes:5242880}")
    private long maxSizeBytes;

    @Override
    public String saveFile(MultipartFile file) throws IOException {
        return saveToLocal(file);
    }

    @Override
    public String saveAvatar(MultipartFile file) throws IOException {
        return saveToLocal(file);
    }

    private String saveToLocal(MultipartFile file) throws IOException {
        Path uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);

        String newFileName = ImageUploadSupport.buildStoredFileName(file, maxSizeBytes);
        Path destination = uploadRoot.resolve(newFileName).normalize();

        if (!destination.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        if (Files.notExists(destination)) {
            file.transferTo(destination.toFile());
        }
        return urlPrefix + newFileName;
    }
}
