package org.zhan.recipe_backend.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UploadFileServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void saveFile_rejectsInvalidUploadsAndStoresValidImages() throws IOException {
        UploadFileServiceImpl service = new UploadFileServiceImpl();
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(service, "urlPrefix", "/images/");
        ReflectionTestUtils.setField(service, "maxSizeBytes", 5L);

        MockMultipartFile emptyFile = new MockMultipartFile("file", "cover.png", "image/png", new byte[0]);
        IllegalArgumentException emptyException = assertThrows(IllegalArgumentException.class, () -> service.saveFile(emptyFile));
        assertEquals("Please upload a non-empty image file.", emptyException.getMessage());

        MockMultipartFile textFile = new MockMultipartFile("file", "cover.txt", "text/plain", new byte[]{1, 2});
        IllegalArgumentException typeException = assertThrows(IllegalArgumentException.class, () -> service.saveFile(textFile));
        assertEquals("Only JPG, PNG, or WEBP images are allowed.", typeException.getMessage());

        MockMultipartFile tooLargeFile = new MockMultipartFile("file", "cover.png", "image/png", new byte[]{1, 2, 3, 4, 5, 6});
        IllegalArgumentException sizeException = assertThrows(IllegalArgumentException.class, () -> service.saveFile(tooLargeFile));
        assertEquals("Image size must be 5 MB or smaller.", sizeException.getMessage());

        ReflectionTestUtils.setField(service, "maxSizeBytes", 10L);
        MockMultipartFile validFile = new MockMultipartFile("file", "cover.png", "image/png", new byte[]{1, 2, 3});
        String imageUrl = service.saveFile(validFile);

        assertTrue(imageUrl.startsWith("/images/"));
        assertEquals(1L, Files.list(tempDir).count());
    }
}
