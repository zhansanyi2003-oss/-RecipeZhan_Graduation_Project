package org.zhan.recipe_backend.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AzureBlobUploadFileServiceImplTest {

    @Test
    void saveFile_uploadsValidImageToConfiguredBlobPrefixAndReturnsBlobUrl() throws Exception {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        when(containerClient.getBlobClient("images/5289df737df57326fcdd22597afb1fac.png")).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("https://recipezhanstorage.blob.core.windows.net/recipe-images/images/cover.png");

        AzureBlobUploadFileServiceImpl service = new AzureBlobUploadFileServiceImpl(containerClient);
        ReflectionTestUtils.setField(service, "blobPrefix", "images/");
        ReflectionTestUtils.setField(service, "maxSizeBytes", 10L);

        MockMultipartFile validFile = new MockMultipartFile("file", "cover.png", "image/png", new byte[]{1, 2, 3});
        String imageUrl = service.saveFile(validFile);

        assertEquals("https://recipezhanstorage.blob.core.windows.net/recipe-images/images/cover.png", imageUrl);
        verify(blobClient).upload(any(), eq(3L), eq(true));
        verify(blobClient).setHttpHeaders(any());
    }

    @Test
    void saveAvatar_uploadsValidImageToAvatarBlobPrefixAndReturnsBlobUrl() throws Exception {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        BlobClient blobClient = mock(BlobClient.class);
        when(containerClient.getBlobClient("avatars/5289df737df57326fcdd22597afb1fac.png")).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn("https://recipezhanstorage.blob.core.windows.net/recipe-images/avatars/avatar.png");

        AzureBlobUploadFileServiceImpl service = new AzureBlobUploadFileServiceImpl(containerClient);
        ReflectionTestUtils.setField(service, "blobPrefix", "images/");
        ReflectionTestUtils.setField(service, "avatarBlobPrefix", "avatars/");
        ReflectionTestUtils.setField(service, "maxSizeBytes", 10L);

        MockMultipartFile validFile = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});
        String imageUrl = service.saveAvatar(validFile);

        assertEquals("https://recipezhanstorage.blob.core.windows.net/recipe-images/avatars/avatar.png", imageUrl);
        verify(blobClient).upload(any(), eq(3L), eq(true));
        verify(blobClient).setHttpHeaders(any());
    }

    @Test
    void saveFile_rejectsInvalidImagesBeforeUploadingToAzure() {
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        AzureBlobUploadFileServiceImpl service = new AzureBlobUploadFileServiceImpl(containerClient);
        ReflectionTestUtils.setField(service, "maxSizeBytes", 5L);

        MockMultipartFile textFile = new MockMultipartFile("file", "cover.txt", "text/plain", new byte[]{1, 2});

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.saveFile(textFile));
        assertEquals("Only JPG, PNG, or WEBP images are allowed.", exception.getMessage());
    }
}
