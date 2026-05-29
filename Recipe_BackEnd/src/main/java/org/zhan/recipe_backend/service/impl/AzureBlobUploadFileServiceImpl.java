package org.zhan.recipe_backend.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zhan.recipe_backend.service.UploadFileService;

import java.io.IOException;

@Service
@ConditionalOnProperty(name = "recipe.storage.type", havingValue = "azure")
public class AzureBlobUploadFileServiceImpl implements UploadFileService {

    private final BlobContainerClient containerClient;

    @Value("${recipe.azure.storage.blob-prefix:images/}")
    private String blobPrefix;

    @Value("${recipe.azure.storage.avatar-blob-prefix:avatars/}")
    private String avatarBlobPrefix;

    @Value("${recipe.file.max-size-bytes:5242880}")
    private long maxSizeBytes;

    public AzureBlobUploadFileServiceImpl(BlobContainerClient containerClient) {
        this.containerClient = containerClient;
    }

    @Override
    public String saveFile(MultipartFile file) throws IOException {
        return saveToAzure(file, blobPrefix);
    }

    @Override
    public String saveAvatar(MultipartFile file) throws IOException {
        return saveToAzure(file, avatarBlobPrefix);
    }

    private String saveToAzure(MultipartFile file, String targetPrefix) throws IOException {
        String newFileName = ImageUploadSupport.buildStoredFileName(file, maxSizeBytes);
        String blobName = normalizeBlobPrefix(targetPrefix) + newFileName;
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        blobClient.upload(file.getInputStream(), file.getSize(), true);
        blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(file.getContentType()));
        return blobClient.getBlobUrl();
    }

    private String normalizeBlobPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }
        String normalized = prefix.replace("\\", "/");
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }
}
