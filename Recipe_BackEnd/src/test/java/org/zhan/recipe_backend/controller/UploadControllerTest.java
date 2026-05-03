package org.zhan.recipe_backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zhan.recipe_backend.service.UploadFileService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UploadControllerTest {

    private UploadFileService uploadFileService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UploadController controller = new UploadController();
        uploadFileService = mock(UploadFileService.class);
        ReflectionTestUtils.setField(controller, "uploadFileService", uploadFileService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void uploadImage_returnsBadRequestWhenServiceRejectsFile() throws Exception {
        when(uploadFileService.saveFile(any())).thenThrow(new IllegalArgumentException("Only images are allowed."));

        mockMvc.perform(multipart("/api/upload")
                        .file("file", "not an image".getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Only images are allowed."));
    }
}
